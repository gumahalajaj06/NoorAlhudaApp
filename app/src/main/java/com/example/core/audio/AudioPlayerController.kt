package com.example.core.audio

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.core.data.ReciterInfo
import com.example.core.data.RecitersCatalog
import com.example.core.data.SurahCatalog
import com.example.core.database.AppDatabase
import com.example.core.database.DownloadedAudioEntity
import com.example.core.database.PreferencesManager
import com.example.core.network.NetworkModule
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Request
import java.io.File

data class AudioPlaybackState(
    val reciter: ReciterInfo = RecitersCatalog.defaultReciters.first(),
    val surahNumber: Int = 1,
    val surahName: String = "الفاتحة",
    val ayahNumber: Int? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1f,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val autoNextSurah: Boolean = true,
    val errorMessage: String? = null,
    val downloadingSurahs: Set<Int> = emptySet()
)

class AudioPlayerController(
    private val context: Context,
    private val db: AppDatabase,
    private val prefs: PreferencesManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var progressJob: Job? = null
    private var usingFallbackUrl = false

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    private fun ensureConnected(onReady: ((MediaController) -> Unit)? = null) {
        val existing = controller
        if (existing != null) {
            onReady?.invoke(existing)
            return
        }
        try {
            val token = SessionToken(context, ComponentName(context, QuranPlaybackService::class.java))
            val future = MediaController.Builder(context, token).buildAsync()
            controllerFuture = future
            future.addListener(
                {
                    try {
                        val mc = future.get()
                        controller = mc
                        mc.volume = _state.value.volume
                        mc.repeatMode = _state.value.repeatMode
                        attachPlayerListener(mc)
                        onReady?.invoke(mc)
                    } catch (e: Exception) {
                        _state.update { it.copy(errorMessage = "تعذر تهيئة مشغل الصوت: ${e.localizedMessage}") }
                    }
                },
                ContextCompat.getMainExecutor(context)
            )
        } catch (e: Exception) {
            _state.update { it.copy(isBuffering = false, errorMessage = "خدمة التشغيل غير متاحة في هذه البيئة.") }
        }
    }

    private fun attachPlayerListener(player: Player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressPolling() else progressJob?.cancel()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = player.duration.coerceAtLeast(0L)
                    )
                }
                if (playbackState == Player.STATE_ENDED && _state.value.repeatMode == Player.REPEAT_MODE_OFF) {
                    val current = _state.value
                    if (current.ayahNumber != null) {
                        val surahInfo = SurahCatalog.getByNumber(current.surahNumber)
                        if (current.ayahNumber < surahInfo.ayahCount) {
                            playAyah(current.reciter, current.surahNumber, current.ayahNumber + 1)
                        } else if (current.autoNextSurah && current.surahNumber < 114) {
                            playAyah(current.reciter, current.surahNumber + 1, 1)
                        }
                    } else if (current.autoNextSurah && current.surahNumber < 114) {
                        playSurah(current.reciter, current.surahNumber + 1)
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val cur = _state.value
                if (!usingFallbackUrl && cur.ayahNumber == null) {
                    val fallback = cur.reciter.buildFallbackSurahUrl(cur.surahNumber)
                    if (fallback != null) {
                        usingFallbackUrl = true
                        playUrl(
                            url = fallback,
                            title = "سورة ${cur.surahName}",
                            artist = cur.reciter.nameArabic
                        )
                        return
                    }
                }
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "تعذر تشغيل التلاوة من هذا الرابط. تحقق من الاتصال أو جرّب قارئاً آخر."
                    )
                }
            }
        })
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let { mc ->
                    _state.update {
                        it.copy(
                            positionMs = mc.currentPosition.coerceAtLeast(0L),
                            durationMs = mc.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(400L)
            }
        }
    }

    fun playSurah(reciter: ReciterInfo, surahNumber: Int) {
        val surah = SurahCatalog.getByNumber(surahNumber)
        usingFallbackUrl = false
        _state.update {
            it.copy(
                reciter = reciter,
                surahNumber = surahNumber,
                surahName = surah.nameArabic,
                ayahNumber = null,
                errorMessage = null,
                isBuffering = true
            )
        }
        scope.launch {
            prefs.saveLastAudio(reciter.id, surahNumber)
            val downloaded = withContext(Dispatchers.IO) {
                db.downloadsDao().getDownload(reciter.id, surahNumber)
            }
            val uriToPlay = if (downloaded != null && File(downloaded.localPath).exists()) {
                Uri.fromFile(File(downloaded.localPath)).toString()
            } else {
                reciter.buildSurahStreamUrl(surahNumber)
            }
            playUrl(
                url = uriToPlay,
                title = "سورة ${surah.nameArabic}",
                artist = reciter.nameArabic
            )
        }
    }

    fun playAyah(reciter: ReciterInfo, surahNumber: Int, ayahNumber: Int) {
        val surah = SurahCatalog.getByNumber(surahNumber)
        val globalAyah = SurahCatalog.getGlobalAyahNumber(surahNumber, ayahNumber)
        val url = reciter.buildAyahStreamUrl(globalAyah)
        if (url == null) {
            _state.update {
                it.copy(errorMessage = "تلاوة الآيات المنفردة غير متاحة لهذا القارئ، يمكنك تشغيل السورة كاملة.")
            }
            return
        }
        usingFallbackUrl = false
        _state.update {
            it.copy(
                reciter = reciter,
                surahNumber = surahNumber,
                surahName = surah.nameArabic,
                ayahNumber = ayahNumber,
                errorMessage = null,
                isBuffering = true
            )
        }
        playUrl(
            url = url,
            title = "سورة ${surah.nameArabic} — آية $ayahNumber",
            artist = reciter.nameArabic
        )
    }

    private fun playUrl(url: String, title: String, artist: String) {
        ensureConnected { mc ->
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setAlbumTitle("القرآن الكريم - نور الهدى")
                        .build()
                )
                .build()
            mc.setMediaItem(mediaItem)
            mc.prepare()
            mc.play()
        }
    }

    fun togglePlayPause() {
        ensureConnected { mc ->
            if (mc.isPlaying) {
                mc.pause()
            } else {
                if (mc.mediaItemCount == 0) {
                    playSurah(_state.value.reciter, _state.value.surahNumber)
                } else {
                    mc.play()
                }
            }
        }
    }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms.coerceAtLeast(0L))
    }

    fun skipForward10s() {
        controller?.let {
            val target = (it.currentPosition + 10_000L).coerceAtMost(it.duration.coerceAtLeast(0L))
            it.seekTo(target)
        }
    }

    fun skipBackward10s() {
        controller?.let {
            val target = (it.currentPosition - 10_000L).coerceAtLeast(0L)
            it.seekTo(target)
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        controller?.volume = clamped
        _state.update { it.copy(volume = clamped) }
    }

    fun toggleRepeatOne() {
        val next = if (_state.value.repeatMode == Player.REPEAT_MODE_ONE) {
            Player.REPEAT_MODE_OFF
        } else {
            Player.REPEAT_MODE_ONE
        }
        controller?.repeatMode = next
        _state.update { it.copy(repeatMode = next) }
    }

    fun toggleAutoNext() {
        _state.update { it.copy(autoNextSurah = !it.autoNextSurah) }
    }

    fun selectReciter(reciter: ReciterInfo) {
        _state.update { it.copy(reciter = reciter, errorMessage = null) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun downloadSurah(reciter: ReciterInfo, surahNumber: Int) {
        if (_state.value.downloadingSurahs.contains(surahNumber)) return
        _state.update { it.copy(downloadingSurahs = it.downloadingSurahs + surahNumber, errorMessage = null) }

        scope.launch(Dispatchers.IO) {
            val audioDir = File(context.filesDir, "audio/${reciter.id}").apply { mkdirs() }
            val padded = surahNumber.toString().padStart(3, '0')
            val destFile = File(audioDir, "$padded.mp3")
            val candidates = listOfNotNull(
                reciter.buildSurahStreamUrl(surahNumber),
                reciter.buildFallbackSurahUrl(surahNumber)
            )
            var success = false
            for (url in candidates) {
                try {
                    val req = Request.Builder().url(url).build()
                    NetworkModule.okHttpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            resp.body?.byteStream()?.use { input ->
                                destFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            if (destFile.exists() && destFile.length() > 4096) {
                                val surah = SurahCatalog.getByNumber(surahNumber)
                                db.downloadsDao().insertDownload(
                                    DownloadedAudioEntity(
                                        id = "${reciter.id}_$surahNumber",
                                        reciterId = reciter.id,
                                        reciterName = reciter.nameArabic,
                                        surahNumber = surahNumber,
                                        surahName = surah.nameArabic,
                                        localPath = destFile.absolutePath,
                                        fileSizeBytes = destFile.length()
                                    )
                                )
                                success = true
                            }
                        }
                    }
                    if (success) break
                } catch (_: Exception) {
                    destFile.delete()
                }
            }
            if (!success) {
                _state.update {
                    it.copy(errorMessage = "تعذر تنزيل السورة حالياً. تأكد من الاتصال بالإنترنت وتوفر المساحة.")
                }
            }
            _state.update { it.copy(downloadingSurahs = it.downloadingSurahs - surahNumber) }
        }
    }

    suspend fun deleteDownload(item: DownloadedAudioEntity) = withContext(Dispatchers.IO) {
        File(item.localPath).delete()
        db.downloadsDao().deleteDownload(item.id)
    }

    fun release() {
        progressJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        scope.cancel()
    }
}
