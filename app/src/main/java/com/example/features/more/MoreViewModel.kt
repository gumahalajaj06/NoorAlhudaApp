package com.example.features.more

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.audio.AudioPlayerController
import com.example.core.database.*
import com.example.core.repositories.QuranRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MoreUiState(
    val prefs: UserPreferences = UserPreferences(),
    val favoriteAyahs: List<FavoriteAyahEntity> = emptyList(),
    val favoriteAzkar: List<FavoriteZikrEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val downloadedAudio: List<DownloadedAudioEntity> = emptyList(),
    val downloadedMushafPages: Int = 0
)

class MoreViewModel(
    private val appContext: Context,
    private val db: AppDatabase,
    private val prefsManager: PreferencesManager,
    private val quranRepository: QuranRepository,
    private val audioPlayerController: AudioPlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoreUiState())
    val uiState: StateFlow<MoreUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsManager.preferencesFlow.collect { prefs ->
                _uiState.update {
                    it.copy(
                        prefs = prefs,
                        downloadedMushafPages = quranRepository.getDownloadedPagesCount()
                    )
                }
            }
        }
        viewModelScope.launch {
            db.favoritesDao().observeFavoriteAyahs().collect { list ->
                _uiState.update { it.copy(favoriteAyahs = list) }
            }
        }
        viewModelScope.launch {
            db.favoritesDao().observeFavoriteAzkar().collect { list ->
                _uiState.update { it.copy(favoriteAzkar = list) }
            }
        }
        viewModelScope.launch {
            db.bookmarksDao().observeBookmarks().collect { list ->
                _uiState.update { it.copy(bookmarks = list) }
            }
        }
        viewModelScope.launch {
            db.downloadsDao().observeDownloads().collect { list ->
                _uiState.update { it.copy(downloadedAudio = list) }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setDarkMode(enabled) }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch { prefsManager.setQuranFontSize(size) }
    }

    fun setArabicFont(fontId: String) {
        viewModelScope.launch { prefsManager.setSelectedArabicFont(fontId) }
    }

    fun setAppLanguage(lang: String) {
        viewModelScope.launch { prefsManager.setAppLanguage(lang) }
    }

    fun setHijriOffset(offset: Int) {
        viewModelScope.launch { prefsManager.setHijriOffset(offset) }
    }

    fun setNotificationToggle(key: String, enabled: Boolean) {
        viewModelScope.launch { prefsManager.setPrayerNotify(key, enabled) }
    }

    fun setAudioVolume(volume: Float) {
        audioPlayerController.setVolume(volume)
    }

    fun removeFavoriteAyah(key: String) {
        viewModelScope.launch { db.favoritesDao().deleteFavoriteAyah(key) }
    }

    fun removeFavoriteZikr(id: String) {
        viewModelScope.launch { db.favoritesDao().deleteFavoriteZikr(id) }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch { quranRepository.deleteBookmark(id) }
    }

    fun deleteDownloadedAudio(item: DownloadedAudioEntity) {
        viewModelScope.launch { audioPlayerController.deleteDownload(item) }
    }

    fun clearMushafPagesCache() {
        viewModelScope.launch {
            quranRepository.clearMushafCache()
            _uiState.update { it.copy(downloadedMushafPages = 0) }
        }
    }

    fun refreshDownloadedMushafCount() {
        _uiState.update { it.copy(downloadedMushafPages = quranRepository.getDownloadedPagesCount()) }
    }
}
