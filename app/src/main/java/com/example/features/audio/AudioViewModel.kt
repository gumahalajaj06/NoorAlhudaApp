package com.example.features.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.audio.AudioPlaybackState
import com.example.core.audio.AudioPlayerController
import com.example.core.data.ReciterInfo
import com.example.core.data.RecitersCatalog
import com.example.core.data.SurahCatalog
import com.example.core.data.SurahInfo
import com.example.core.database.AppDatabase
import com.example.core.database.DownloadedAudioEntity
import com.example.core.network.NetworkModule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AudioUiState(
    val reciters: List<ReciterInfo> = RecitersCatalog.defaultReciters,
    val surahs: List<SurahInfo> = SurahCatalog.surahs,
    val surahSearchQuery: String = "",
    val downloadedSurahKeys: Set<String> = emptySet(),
    val downloadedItems: List<DownloadedAudioEntity> = emptyList(),
    val isRefreshingReciters: Boolean = false
)

class AudioViewModel(
    private val playerController: AudioPlayerController,
    private val db: AppDatabase
) : ViewModel() {

    val playbackState: StateFlow<AudioPlaybackState> = playerController.state

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            db.downloadsDao().observeDownloads().collect { list ->
                _uiState.update {
                    it.copy(
                        downloadedItems = list,
                        downloadedSurahKeys = list.map { d -> "${d.reciterId}_${d.surahNumber}" }.toSet()
                    )
                }
            }
        }
        refreshRecitersFromMp3Quran()
    }

    fun refreshRecitersFromMp3Quran() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingReciters = true) }
            try {
                val res = NetworkModule.mp3QuranApi.getReciters("ar")
                val remoteReciters = res.reciters.mapNotNull { dto ->
                    val moshaf = dto.moshaf.firstOrNull { it.surahTotal >= 114 && it.server.isNotBlank() }
                        ?: return@mapNotNull null
                    val cleanName = dto.name.trim()
                    if (RecitersCatalog.defaultReciters.any { it.nameArabic == cleanName }) return@mapNotNull null
                    ReciterInfo(
                        id = "mp3q_${dto.id}",
                        nameArabic = cleanName,
                        styleArabic = moshaf.name.ifBlank { "حفص عن عاصم" },
                        mp3QuranServerUrl = moshaf.server,
                        islamicNetworkEdition = null,
                        supportsAyahByAyah = false
                    )
                }.take(20)
                if (remoteReciters.isNotEmpty()) {
                    _uiState.update {
                        it.copy(reciters = RecitersCatalog.defaultReciters + remoteReciters)
                    }
                }
            } catch (_: Exception) {
                // Keep built-in verified reciters
            } finally {
                _uiState.update { it.copy(isRefreshingReciters = false) }
            }
        }
    }

    fun onSurahSearch(query: String) {
        val q = query.trim()
        val filtered = if (q.isEmpty()) {
            SurahCatalog.surahs
        } else {
            SurahCatalog.surahs.filter {
                it.nameArabic.contains(q) || it.number.toString() == q
            }
        }
        _uiState.update { it.copy(surahSearchQuery = query, surahs = filtered) }
    }

    fun selectReciter(reciter: ReciterInfo) = playerController.selectReciter(reciter)
    fun playSurah(surahNumber: Int) = playerController.playSurah(playbackState.value.reciter, surahNumber)
    fun playAyah(surahNumber: Int, ayahNumber: Int) = playerController.playAyah(playbackState.value.reciter, surahNumber, ayahNumber)
    fun togglePlayPause() = playerController.togglePlayPause()
    fun seekTo(ms: Long) = playerController.seekTo(ms)
    fun skipForward10s() = playerController.skipForward10s()
    fun skipBackward10s() = playerController.skipBackward10s()
    fun setVolume(vol: Float) = playerController.setVolume(vol)
    fun toggleRepeatOne() = playerController.toggleRepeatOne()
    fun toggleAutoNext() = playerController.toggleAutoNext()
    fun clearError() = playerController.clearError()

    fun downloadSurah(surahNumber: Int) {
        playerController.downloadSurah(playbackState.value.reciter, surahNumber)
    }

    fun deleteDownload(item: DownloadedAudioEntity) {
        viewModelScope.launch {
            playerController.deleteDownload(item)
        }
    }
}
