package com.example.features.mushaf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.SurahCatalog
import com.example.core.database.BookmarkEntity
import com.example.core.database.PreferencesManager
import com.example.core.repositories.QuranRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MushafUiState(
    val currentPage: Int = 1,
    val currentSurahName: String = "الفاتحة",
    val currentSurahNumber: Int = 1,
    val isCurrentPageBookmarked: Boolean = false,
    val isCurrentPageDownloaded: Boolean = false,
    val downloadedCount: Int = 0,
    val isDownloading: Boolean = false,
    val statusMessage: String? = null,
    val bookmarks: List<BookmarkEntity> = emptyList()
)

class MushafViewModel(
    private val quranRepository: QuranRepository,
    private val prefsManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MushafUiState())
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = prefsManager.preferencesFlow.first()
            updatePageState(prefs.lastMushafPage.coerceIn(1, 604))
        }

        viewModelScope.launch {
            quranRepository.observeBookmarks().collect { list ->
                val mushafBookmarks = list.filter { it.type == "MUSHAF" }
                val curPage = _uiState.value.currentPage
                _uiState.update {
                    it.copy(
                        bookmarks = mushafBookmarks,
                        isCurrentPageBookmarked = mushafBookmarks.any { b -> b.pageNumber == curPage }
                    )
                }
            }
        }
    }

    fun onPageChanged(page: Int) {
        val safePage = page.coerceIn(1, 604)
        updatePageState(safePage)
        viewModelScope.launch {
            prefsManager.saveLastMushafPage(safePage)
        }
    }

    private fun updatePageState(page: Int) {
        val surah = SurahCatalog.getSurahForPage(page)
        val localFile = quranRepository.getLocalMushafPageFile(page)
        val isDownloaded = localFile.exists() && localFile.length() > 1024
        val bookmarked = _uiState.value.bookmarks.any { it.pageNumber == page }
        _uiState.update {
            it.copy(
                currentPage = page,
                currentSurahName = surah.nameArabic,
                currentSurahNumber = surah.number,
                isCurrentPageBookmarked = bookmarked,
                isCurrentPageDownloaded = isDownloaded,
                downloadedCount = quranRepository.getDownloadedPagesCount()
            )
        }
    }

    fun getPageModelForCoil(page: Int): Any {
        val local = quranRepository.getLocalMushafPageFile(page)
        return if (local.exists() && local.length() > 1024) {
            local
        } else {
            quranRepository.getMushafPageUrls(page).first()
        }
    }

    fun getFallbackPageUrl(page: Int): String = quranRepository.getMushafPageUrls(page)[1]

    fun toggleBookmarkCurrentPage() {
        val cur = _uiState.value
        viewModelScope.launch {
            quranRepository.toggleMushafBookmark(cur.currentPage, cur.isCurrentPageBookmarked)
        }
    }

    fun downloadCurrentPage() {
        val page = _uiState.value.currentPage
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, statusMessage = null) }
            val ok = quranRepository.downloadMushafPage(page)
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    isCurrentPageDownloaded = ok,
                    downloadedCount = quranRepository.getDownloadedPagesCount(),
                    statusMessage = if (ok) "تم حفظ الصفحة $page للعمل دون إنترنت" else "تعذر تحميل الصفحة حالياً"
                )
            }
        }
    }

    fun downloadSurahPages(surahNumber: Int) {
        val surah = SurahCatalog.getByNumber(surahNumber)
        val nextSurah = if (surahNumber < 114) SurahCatalog.getByNumber(surahNumber + 1) else null
        val start = surah.startPage
        val end = ((nextSurah?.startPage ?: 604) - if (nextSurah != null && nextSurah.startPage > start) 1 else 0).coerceIn(start, 604)

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true, statusMessage = "جارٍ تنزيل صفحات سورة ${surah.nameArabic}...") }
            var saved = 0
            for (p in start..end) {
                if (quranRepository.downloadMushafPage(p)) saved++
            }
            _uiState.update {
                it.copy(
                    isDownloading = false,
                    downloadedCount = quranRepository.getDownloadedPagesCount(),
                    isCurrentPageDownloaded = quranRepository.getLocalMushafPageFile(it.currentPage).exists(),
                    statusMessage = "تم تنزيل $saved صفحة لسورة ${surah.nameArabic}"
                )
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
