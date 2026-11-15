package com.example.features.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.core.audio.AudioPlaybackState
import com.example.core.data.ReciterInfo
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily

@Composable
fun AudioPlayerScreen(
    uiState: AudioUiState,
    playback: AudioPlaybackState,
    onSelectReciter: (ReciterInfo) -> Unit,
    onSearchSurah: (String) -> Unit,
    onPlaySurah: (Int) -> Unit,
    onPlayAyah: (Int, Int) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleAutoNext: () -> Unit,
    onDownloadSurah: (Int) -> Unit,
    onClearError: () -> Unit,
    onGoHome: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    var showAyahPlayDialog by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
    ) {
        NoorTopBar(
            title = "التلاوة الصوتية",
            subtitle = "تشغيل في الخلفية ودعم شاشة القفل",
            actions = {
                IconButton(onClick = onGoHome, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(Icons.Outlined.Home, contentDescription = "الرئيسية", tint = noorColors.gold)
                }
            }
        )

        // Luxury Now Playing Card
        NoorGoldCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(noorColors.emerald.copy(alpha = 0.85f), Color(0xFF102849))
                        )
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (playback.ayahNumber != null) {
                                    "سورة ${playback.surahName} — آية ${playback.ayahNumber}"
                                } else {
                                    "سورة ${playback.surahName}"
                                },
                                fontFamily = LocalScriptFontFamily.current,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFFF7F5EF),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = playback.reciter.nameArabic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE7CE78)
                            )
                        }
                        if (playback.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color(0xFFD6B43F),
                                strokeWidth = 2.5.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val duration = playback.durationMs.coerceAtLeast(1L)
                    Slider(
                        value = playback.positionMs.coerceIn(0L, duration).toFloat(),
                        onValueChange = { onSeekTo(it.toLong()) },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFD6B43F),
                            activeTrackColor = Color(0xFFD6B43F)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(playback.positionMs), style = MaterialTheme.typography.labelSmall, color = Color(0xFFF7F5EF))
                        Text(formatTime(playback.durationMs), style = MaterialTheme.typography.labelSmall, color = Color(0xFFF7F5EF))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Playback Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleRepeat) {
                            Icon(
                                imageVector = Icons.Filled.RepeatOne,
                                contentDescription = "تكرار",
                                tint = if (playback.repeatMode == Player.REPEAT_MODE_ONE) Color(0xFFD6B43F) else Color(0xFFB7C4D8)
                            )
                        }
                        IconButton(onClick = onSkipBackward) {
                            Icon(Icons.Filled.Replay10, contentDescription = "تراجع 10 ثوان", tint = Color(0xFFF7F5EF), modifier = Modifier.size(28.dp))
                        }
                        FloatingActionButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier.testTag("audio_play_pause"),
                            containerColor = Color(0xFFD6B43F),
                            contentColor = Color(0xFF091629)
                        ) {
                            Icon(
                                imageVector = if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (playback.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        IconButton(onClick = onSkipForward) {
                            Icon(Icons.Filled.Forward10, contentDescription = "تقديم 10 ثوان", tint = Color(0xFFF7F5EF), modifier = Modifier.size(28.dp))
                        }
                        IconButton(onClick = onToggleAutoNext) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "تشغيل تلقائي للسورة التالية",
                                tint = if (playback.autoNextSurah) Color(0xFFD6B43F) else Color(0xFFB7C4D8)
                            )
                        }
                    }

                    // Volume Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.VolumeDown, contentDescription = null, tint = Color(0xFFE7CE78), modifier = Modifier.size(18.dp))
                        Slider(
                            value = playback.volume,
                            onValueChange = onVolumeChange,
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f).padding(horizontal = 6.dp)
                        )
                        Icon(Icons.Outlined.VolumeUp, contentDescription = null, tint = Color(0xFFE7CE78), modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (playback.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = playback.errorMessage,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearError) { Text("إغلاق") }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reciters Horizontal Picker
        Text(
            text = "القراء",
            style = MaterialTheme.typography.titleMedium,
            color = noorColors.gold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.reciters, key = { it.id }) { reciter ->
                val selected = reciter.id == playback.reciter.id
                FilterChip(
                    selected = selected,
                    onClick = { onSelectReciter(reciter) },
                    label = { Text(reciter.nameArabic) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = noorColors.emerald,
                        selectedLabelColor = Color(0xFFF7F5EF)
                    )
                )
            }
        }

        OutlinedTextField(
            value = uiState.surahSearchQuery,
            onValueChange = onSearchSurah,
            placeholder = { Text("ابحث عن سورة للتلاوة...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = noorColors.gold) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.surahs, key = { it.number }) { surah ->
                val isPlayingThis = playback.surahNumber == surah.number && playback.isPlaying
                val isDownloaded = uiState.downloadedSurahKeys.contains("${playback.reciter.id}_${surah.number}")
                val isDownloading = playback.downloadingSurahs.contains(surah.number)

                NoorGoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onPlaySurah(surah.number) },
                    borderColor = if (playback.surahNumber == surah.number) noorColors.gold else noorColors.goldBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(noorColors.emerald.copy(alpha = 0.25f))
                                .border(1.dp, noorColors.goldBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = surah.number.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = noorColors.gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${surah.nameArabic}",
                                fontFamily = LocalScriptFontFamily.current,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${surah.ayahCount} آية • ${surah.revelationType}",
                                style = MaterialTheme.typography.labelSmall,
                                color = noorColors.mutedText
                            )
                        }
                        if (playback.reciter.supportsAyahByAyah) {
                            IconButton(onClick = { showAyahPlayDialog = surah.number }) {
                                Icon(Icons.Outlined.FormatListNumbered, contentDescription = "تشغيل آية محددة", tint = noorColors.gold)
                            }
                        }
                        IconButton(
                            onClick = { onDownloadSurah(surah.number) },
                            enabled = !isDownloading && !isDownloaded
                        ) {
                            when {
                                isDownloading -> CircularProgressIndicator(modifier = Modifier.size(18.dp), color = noorColors.gold)
                                isDownloaded -> Icon(Icons.Filled.DownloadDone, contentDescription = "تم التنزيل", tint = noorColors.emeraldBright)
                                else -> Icon(Icons.Outlined.Download, contentDescription = "تنزيل السورة", tint = noorColors.gold)
                            }
                        }
                        IconButton(onClick = {
                            if (playback.surahNumber == surah.number) onTogglePlayPause()
                            else onPlaySurah(surah.number)
                        }) {
                            Icon(
                                imageVector = if (isPlayingThis) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                                contentDescription = "تشغيل السورة",
                                tint = noorColors.gold,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    showAyahPlayDialog?.let { surahNum ->
        val surahInfo = uiState.surahs.firstOrNull { it.number == surahNum }
        var ayahText by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showAyahPlayDialog = null },
            title = { Text("تشغيل آية محددة من سورة ${surahInfo?.nameArabic ?: ""}") },
            text = {
                OutlinedTextField(
                    value = ayahText,
                    onValueChange = { if (it.all(Char::isDigit)) ayahText = it },
                    label = { Text("رقم الآية (1 - ${surahInfo?.ayahCount ?: 1})") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val maxAyah = surahInfo?.ayahCount ?: 1
                    val ayahNo = (ayahText.toIntOrNull() ?: 1).coerceIn(1, maxAyah)
                    showAyahPlayDialog = null
                    onPlayAyah(surahNum, ayahNo)
                }) { Text("تشغيل") }
            },
            dismissButton = {
                TextButton(onClick = { showAyahPlayDialog = null }) { Text("إلغاء") }
            }
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSec / 3600L
    val mins = (totalSec % 3600L) / 60L
    val secs = totalSec % 60L
    return if (hours > 0) "%d:%02d:%02d".format(hours, mins, secs)
    else "%02d:%02d".format(mins, secs)
}
