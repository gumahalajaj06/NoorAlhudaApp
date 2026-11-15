package com.example.features.quran

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.CachedAyahEntity
import com.example.ui.components.ArabicFontLivePreviewCard
import com.example.ui.components.ArabicFontOptionsList
import com.example.ui.components.BasmalahOrnament
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.ArabicScriptFont
import com.example.ui.theme.IvoryWhite
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily
import com.example.ui.theme.NotoNaskhArabicFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReaderScreen(
    surahNumber: Int,
    targetAyah: Int,
    state: SurahReaderUiState,
    onLoadSurah: (Int) -> Unit,
    onBack: () -> Unit,
    onBookmarkAyah: (Int, Int) -> Unit,
    onToggleFavorite: (CachedAyahEntity) -> Unit,
    onSelectTafsir: (CachedAyahEntity?) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onFontFamilyChange: (String) -> Unit,
    onToggleNightMode: () -> Unit,
    onPlayAyahAudio: (Int, Int) -> Unit,
    onOpenInMushaf: (Int) -> Unit
) {
    val context = LocalContext.current
    val noorColors = LocalNoorColors.current
    val scriptFontFamily = LocalScriptFontFamily.current
    val activeScriptFont = ArabicScriptFont.fromId(state.selectedFontId)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showFontSheet by remember { mutableStateOf(false) }

    LaunchedEffect(surahNumber) {
        onLoadSurah(surahNumber)
    }

    LaunchedEffect(state.ayahs.size, targetAyah) {
        if (state.ayahs.isNotEmpty() && targetAyah > 1) {
            val idx = (targetAyah - 1).coerceIn(0, state.ayahs.lastIndex)
            listState.scrollToItem(idx + 1)
        }
    }

    val readerBg = if (state.readerNightMode) Color(0xFF091629) else IvoryWhite
    val readerCardBg = if (state.readerNightMode) Color(0xFF192F50) else Color.White
    val readerText = if (state.readerNightMode) IvoryWhite else Color(0xFF091629)

    Scaffold(
        containerColor = readerBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 680.dp)
        ) {
            NoorTopBar(
                title = "سورة ${state.surahInfo.nameArabic}",
                subtitle = "${state.surahInfo.revelationType} • ${state.surahInfo.ayahCount} آية",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showJumpDialog = true }) {
                        Icon(Icons.Outlined.FormatListNumbered, contentDescription = "الانتقال لآية", tint = noorColors.gold)
                    }
                    IconButton(onClick = onToggleNightMode) {
                        Icon(
                            imageVector = if (state.readerNightMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "تبديل وضع القراءة",
                            tint = noorColors.gold
                        )
                    }
                    IconButton(onClick = { onOpenInMushaf(state.surahInfo.startPage) }) {
                        Icon(Icons.Outlined.MenuBook, contentDescription = "عرض في المصحف", tint = noorColors.gold)
                    }
                }
            )

            // Font size & calligraphy bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(readerCardBg.copy(alpha = 0.7f))
                    .border(1.dp, noorColors.goldBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الحجم", style = MaterialTheme.typography.labelMedium, color = noorColors.gold)
                Spacer(modifier = Modifier.width(6.dp))
                Slider(
                    value = state.fontSizeSp,
                    onValueChange = onFontSizeChange,
                    valueRange = 18f..36f,
                    modifier = Modifier.weight(1f)
                )
                Text("${state.fontSizeSp.toInt()}", style = MaterialTheme.typography.labelMedium, color = readerText)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = { showFontSheet = true },
                    shape = RoundedCornerShape(50),
                    color = noorColors.emerald.copy(alpha = 0.25f),
                    border = BorderStroke(0.8.dp, noorColors.goldBorder),
                    modifier = Modifier.testTag("reader_font_picker_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TextFields,
                            contentDescription = "تغيير الخط",
                            tint = noorColors.gold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activeScriptFont.nameArabic,
                            style = MaterialTheme.typography.labelSmall,
                            color = noorColors.gold,
                            maxLines = 1
                        )
                    }
                }
            }

            when {
                state.isLoading && state.ayahs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = noorColors.gold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("جارٍ تحميل السورة بالرسم العثماني...", color = readerText)
                        }
                    }
                }
                state.errorMessage != null && state.ayahs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = noorColors.gold, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(state.errorMessage, color = readerText, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(onClick = { onLoadSurah(surahNumber) }) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (surahNumber != 1 && surahNumber != 9) {
                            item {
                                BasmalahOrnament(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }

                        items(state.ayahs, key = { it.ayahNumber }) { ayah ->
                            val isFav = state.favoriteKeys.contains("${ayah.surahNumber}_${ayah.ayahNumber}")
                            NoorGoldCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ayah_card_${ayah.ayahNumber}"),
                                containerColor = readerCardBg
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(noorColors.emerald.copy(alpha = 0.25f))
                                            .border(1.dp, noorColors.goldBorder, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ayah.ayahNumber.toString(),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = noorColors.gold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(onClick = { onPlayAyahAudio(ayah.surahNumber, ayah.ayahNumber) }) {
                                            Icon(Icons.Outlined.VolumeUp, contentDescription = "استماع للآية", tint = noorColors.gold)
                                        }
                                        IconButton(onClick = { onSelectTafsir(ayah) }) {
                                            Icon(Icons.Outlined.MenuBook, contentDescription = "التفسير", tint = noorColors.gold)
                                        }
                                        IconButton(onClick = { onToggleFavorite(ayah) }) {
                                            Icon(
                                                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "مفضلة",
                                                tint = if (isFav) Color(0xFFE57373) else noorColors.mutedText
                                            )
                                        }
                                        IconButton(onClick = {
                                            onBookmarkAyah(ayah.surahNumber, ayah.ayahNumber)
                                            scope.launch { snackbarHostState.showSnackbar("تم حفظ موضع القراءة: آية ${ayah.ayahNumber}") }
                                        }) {
                                            Icon(Icons.Outlined.BookmarkAdd, contentDescription = "حفظ موضع القراءة", tint = noorColors.gold)
                                        }
                                        IconButton(onClick = {
                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            cm.setPrimaryClip(ClipData.newPlainText("Ayah", "${ayah.textUthmani} [${state.surahInfo.nameArabic}: ${ayah.ayahNumber}]"))
                                            scope.launch { snackbarHostState.showSnackbar("تم نسخ الآية الكريمة") }
                                        }) {
                                            Icon(Icons.Outlined.ContentCopy, contentDescription = "نسخ", tint = noorColors.mutedText)
                                        }
                                        IconButton(onClick = {
                                            val share = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, "${ayah.textUthmani}\n[سورة ${state.surahInfo.nameArabic}: ${ayah.ayahNumber}] — تطبيق نور الهدى")
                                            }
                                            context.startActivity(Intent.createChooser(share, "مشاركة الآية"))
                                        }) {
                                            Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = noorColors.mutedText)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${ayah.textUthmani} ﴿${ayah.ayahNumber}﴾",
                                    fontFamily = scriptFontFamily,
                                    fontSize = state.fontSizeSp.sp,
                                    lineHeight = (state.fontSizeSp * 1.75f).sp,
                                    color = readerText,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Next / Prev Surah navigation
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (surahNumber > 1) {
                                    OutlinedButton(
                                        onClick = { onLoadSurah(surahNumber - 1) },
                                        modifier = Modifier.weight(1f),
                                        border = BorderStroke(1.dp, noorColors.gold)
                                    ) {
                                        Text("السورة السابقة", color = noorColors.gold)
                                    }
                                }
                                if (surahNumber < 114) {
                                    Button(
                                        onClick = { onLoadSurah(surahNumber + 1) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = noorColors.emerald)
                                    ) {
                                        Text("السورة التالية")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Tafsir Bottom Sheet
    state.selectedAyahForTafsir?.let { ayah ->
        ModalBottomSheet(onDismissRequest = { onSelectTafsir(null) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "التفسير الميسّر — سورة ${state.surahInfo.nameArabic} (آية ${ayah.ayahNumber})",
                    style = MaterialTheme.typography.titleLarge,
                    color = noorColors.gold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ayah.textUthmani,
                    fontFamily = scriptFontFamily,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(color = noorColors.goldBorder)
                Text(
                    text = ayah.tafsirMuyassar,
                    fontFamily = NotoNaskhArabicFontFamily,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showFontSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFontSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تخصيص خط المصحف والتلاوة",
                        style = MaterialTheme.typography.titleLarge,
                        color = noorColors.gold,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showFontSheet = false },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("font_sheet_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "إغلاق",
                            tint = noorColors.gold
                        )
                    }
                }
                ArabicFontLivePreviewCard(
                    selectedFontId = state.selectedFontId,
                    previewFontSizeSp = state.fontSizeSp.coerceAtMost(28f)
                )
                ArabicFontOptionsList(
                    selectedFontId = state.selectedFontId,
                    onSelectFont = { fontId -> onFontFamilyChange(fontId) },
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (showJumpDialog) {
        var ayahInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("الانتقال إلى رقم آية") },
            text = {
                OutlinedTextField(
                    value = ayahInput,
                    onValueChange = { if (it.all(Char::isDigit)) ayahInput = it },
                    label = { Text("رقم الآية (1 - ${state.surahInfo.ayahCount})") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val num = ayahInput.toIntOrNull()?.coerceIn(1, state.surahInfo.ayahCount) ?: 1
                    showJumpDialog = false
                    scope.launch {
                        val idx = (num - 1).coerceIn(0, (state.ayahs.size - 1).coerceAtLeast(0))
                        listState.animateScrollToItem(idx)
                    }
                }) { Text("انتقال") }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
