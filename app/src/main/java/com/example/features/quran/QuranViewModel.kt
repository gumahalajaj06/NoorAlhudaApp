package com.example.features.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.SurahCatalog
import com.example.core.data.SurahInfo
import com.example.core.database.CachedAyahEntity
import com.example.core.database.PreferencesManager
import com.example.core.repositories.AyahSearchHit
import com.example.core.repositories.QuranRepository
import com.example.ui.theme.ArabicScriptFont
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuranIndexUiState(
    val searchQuery: String = "",
    val surahs: List<SurahInfo> = SurahCatalog.surahs,
    val ayahSearchHits: List<AyahSearchHit> = emptyList(),
    val isSearchingAyahs: Boolean = false,
    val lastReadSurah: Int = 1,
    val lastReadAyah: Int = 1,
    val lastReadSurahName: String = "الفاتحة",
    val filterType: String = "الكل" // "الكل", "مكية", "مدنية"
)

data class SurahReaderUiState(
    val surahInfo: SurahInfo = SurahCatalog.getByNumber(1),
    val ayahs: List<CachedAyahEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val fontSizeSp: Float = 25f,
    val selectedFontId: String = ArabicScriptFont.AMIRI.id,
    val readerNightMode: Boolean = true,
    val favoriteKeys: Set<String> = emptySet(),
    val selectedAyahForTafsir: CachedAyahEntity? = null
)

class QuranViewModel(
    private val quranRepository: QuranRepository,
    private val prefsManager: PreferencesManager
) : ViewModel() {

    private val _indexState = MutableStateFlow(QuranIndexUiState())
    val indexState: StateFlow<QuranIndexUiState> = _indexState.asStateFlow()

    private val _readerState = MutableStateFlow(SurahReaderUiState())
    val readerState: StateFlow<SurahReaderUiState> = _readerState.asStateFlow()

    private var surahObserverJob: Job? = null
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            prefsManager.preferencesFlow.collect { prefs ->
                _indexState.update {
                    it.copy(
                        lastReadSurah = prefs.lastReadSurah,
                        lastReadAyah = prefs.lastReadAyah,
                        lastReadSurahName = prefs.lastReadSurahName
                    )
                }
                _readerState.update {
                    it.copy(
                        fontSizeSp = prefs.quranFontSize,
                        selectedFontId = prefs.selectedArabicFont,
                        readerNightMode = prefs.isDarkMode
                    )
                }
            }
        }

        viewModelScope.launch {
            quranRepository.observeFavoriteAyahs().collect { favs ->
                _readerState.update { state ->
                    state.copy(favoriteKeys = favs.map { it.key }.toSet())
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _indexState.update { it.copy(searchQuery = query) }
        applySurahFilter(query, _indexState.value.filterType)

        searchJob?.cancel()
        if (query.trim().length >= 2) {
            searchJob = viewModelScope.launch {
                _indexState.update { it.copy(isSearchingAyahs = true) }
                val hits = quranRepository.searchAyahs(query)
                _indexState.update { it.copy(ayahSearchHits = hits, isSearchingAyahs = false) }
            }
        } else {
            _indexState.update { it.copy(ayahSearchHits = emptyList(), isSearchingAyahs = false) }
        }
    }

    fun onFilterTypeChange(type: String) {
        _indexState.update { it.copy(filterType = type) }
        applySurahFilter(_indexState.value.searchQuery, type)
    }

    private fun applySurahFilter(query: String, type: String) {
        val q = query.trim()
        val filtered = SurahCatalog.surahs.filter { surah ->
            val matchesType = type == "الكل" || surah.revelationType == type
            val matchesQuery = q.isEmpty() ||
                surah.nameArabic.contains(q) ||
                surah.nameEnglish.contains(q, ignoreCase = true) ||
                surah.number.toString() == q
            matchesType && matchesQuery
        }
        _indexState.update { it.copy(surahs = filtered) }
    }

    fun openSurah(surahNumber: Int) {
        val info = SurahCatalog.getByNumber(surahNumber)
        _readerState.update {
            it.copy(
                surahInfo = info,
                isLoading = true,
                errorMessage = null,
                selectedAyahForTafsir = null
            )
        }

        surahObserverJob?.cancel()
        surahObserverJob = viewModelScope.launch {
            quranRepository.observeSurahAyahs(surahNumber).collect { cached ->
                if (cached.isNotEmpty()) {
                    _readerState.update { it.copy(ayahs = cached, isLoading = false) }
                }
            }
        }

        viewModelScope.launch {
            val result = quranRepository.ensureSurahLoaded(surahNumber)
            result.onFailure { err ->
                if (_readerState.value.ayahs.isEmpty()) {
                    _readerState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = err.localizedMessage ?: "تعذر تحميل السورة، تحقق من الإنترنت."
                        )
                    }
                }
            }
            prefsManager.saveLastReadQuran(surahNumber, 1, info.nameArabic)
        }
    }

    fun saveLastReadAyah(surahNumber: Int, ayahNumber: Int) {
        val info = SurahCatalog.getByNumber(surahNumber)
        viewModelScope.launch {
            prefsManager.saveLastReadQuran(surahNumber, ayahNumber, info.nameArabic)
            quranRepository.bookmarkAyah(surahNumber, ayahNumber)
        }
    }

    fun toggleFavoriteAyah(ayah: CachedAyahEntity) {
        val key = "${ayah.surahNumber}_${ayah.ayahNumber}"
        val isFav = _readerState.value.favoriteKeys.contains(key)
        viewModelScope.launch {
            quranRepository.toggleFavoriteAyah(
                surahNumber = ayah.surahNumber,
                ayahNumber = ayah.ayahNumber,
                text = ayah.textUthmani,
                currentlyFavorite = isFav
            )
        }
    }

    fun selectAyahForTafsir(ayah: CachedAyahEntity?) {
        _readerState.update { it.copy(selectedAyahForTafsir = ayah) }
    }

    fun updateFontSize(newSize: Float) {
        viewModelScope.launch {
            prefsManager.setQuranFontSize(newSize)
        }
    }

    fun updateArabicFont(fontId: String) {
        viewModelScope.launch {
            prefsManager.setSelectedArabicFont(fontId)
        }
    }

    fun toggleReaderNightMode() {
        _readerState.update { it.copy(readerNightMode = !it.readerNightMode) }
    }
}
