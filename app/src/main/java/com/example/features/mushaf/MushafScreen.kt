package com.example.features.mushaf

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.core.data.SurahCatalog
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import kotlinx.coroutines.launch

@Composable
fun MushafScreen(
    initialPage: Int,
    state: MushafUiState,
    getPageModel: (Int) -> Any,
    getFallbackUrl: (Int) -> String,
    onPageChanged: (Int) -> Unit,
    onToggleBookmark: () -> Unit,
    onDownloadCurrentPage: () -> Unit,
    onDownloadSurahPages: (Int) -> Unit,
    onClearStatus: () -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val startPage = if (initialPage in 1..604) initialPage else state.currentPage
    val pagerState = rememberPagerState(
        initialPage = (startPage - 1).coerceIn(0, 603),
        pageCount = { 604 }
    )

    LaunchedEffect(initialPage) {
        if (initialPage in 1..604 && pagerState.currentPage != initialPage - 1) {
            pagerState.scrollToPage(initialPage - 1)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage + 1)
    }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearStatus()
        }
    }

    var showPageDialog by remember { mutableStateOf(false) }
    var showSurahDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0A1424)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 720.dp)
        ) {
            NoorTopBar(
                title = "المصحف المدني — صفحة ${state.currentPage}",
                subtitle = "سورة ${state.currentSurahName} • محفوظ ${state.downloadedCount}/604",
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.testTag("mushaf_bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (state.isCurrentPageBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "علامة مرجعية",
                            tint = noorColors.gold
                        )
                    }
                    IconButton(onClick = { showBookmarksDialog = true }) {
                        Icon(Icons.Outlined.Bookmarks, contentDescription = "العلامات المحفوظة", tint = noorColors.gold)
                    }
                    IconButton(
                        onClick = onDownloadCurrentPage,
                        enabled = !state.isDownloading
                    ) {
                        Icon(
                            imageVector = if (state.isCurrentPageDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                            contentDescription = "تنزيل الصفحة",
                            tint = if (state.isCurrentPageDownloaded) noorColors.emeraldBright else noorColors.gold
                        )
                    }
                }
            )

            // Quick navigation bar: Jump to Page / Jump to Surah / Download Surah
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showPageDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("رقم الصفحة", color = noorColors.gold, style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { showSurahDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("اختيار السورة", color = noorColors.gold, style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { onDownloadSurahPages(state.currentSurahNumber) },
                    enabled = !state.isDownloading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (state.isDownloading) "جارٍ الحفظ..." else "حفظ السورة",
                        color = noorColors.gold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Lazy-loaded 604-page HorizontalPager with pinch-to-zoom & double-tap zoom
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) { pageIndex ->
                val pageNumber = pageIndex + 1
                ZoomableMushafPage(
                    pageNumber = pageNumber,
                    primaryModel = getPageModel(pageNumber),
                    fallbackUrl = getFallbackUrl(pageNumber)
                )
            }
        }
    }

    if (showPageDialog) {
        var pageInput by remember { mutableStateOf(state.currentPage.toString()) }
        AlertDialog(
            onDismissRequest = { showPageDialog = false },
            title = { Text("الانتقال إلى صفحة") },
            text = {
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { if (it.all(Char::isDigit)) pageInput = it },
                    label = { Text("رقم الصفحة (1 - 604)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val p = (pageInput.toIntOrNull() ?: 1).coerceIn(1, 604)
                    showPageDialog = false
                    scope.launch { pagerState.scrollToPage(p - 1) }
                }) { Text("انتقال") }
            },
            dismissButton = { TextButton(onClick = { showPageDialog = false }) { Text("إلغاء") } }
        )
    }

    if (showSurahDialog) {
        AlertDialog(
            onDismissRequest = { showSurahDialog = false },
            title = { Text("الانتقال إلى سورة") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(SurahCatalog.surahs) { surah ->
                        TextButton(
                            onClick = {
                                showSurahDialog = false
                                scope.launch { pagerState.scrollToPage((surah.startPage - 1).coerceIn(0, 603)) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${surah.number}. سورة ${surah.nameArabic}")
                                Text("ص ${surah.startPage}", color = noorColors.gold)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSurahDialog = false }) { Text("إغلاق") } }
        )
    }

    if (showBookmarksDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarksDialog = false },
            title = { Text("العلامات المرجعية في المصحف") },
            text = {
                if (state.bookmarks.isEmpty()) {
                    Text("لا توجد علامات مرجعية محفوظة بعد.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(state.bookmarks) { bm ->
                            TextButton(
                                onClick = {
                                    showBookmarksDialog = false
                                    scope.launch { pagerState.scrollToPage((bm.pageNumber - 1).coerceIn(0, 603)) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(bm.title, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showBookmarksDialog = false }) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun ZoomableMushafPage(
    pageNumber: Int,
    primaryModel: Any,
    fallbackUrl: String
) {
    val context = LocalContext.current
    val noorColors = LocalNoorColors.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var useFallback by remember(pageNumber) { mutableStateOf(false) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        val nextScale = (scale * zoomChange).coerceIn(1f, 3f)
        scale = nextScale
        offset = if (nextScale > 1f) {
            Offset(
                x = (offset.x + panChange.x).coerceIn(-350f, 350f),
                y = (offset.y + panChange.y).coerceIn(-500f, 500f)
            )
        } else {
            Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF6E8), RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1.1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 2f
                        }
                    }
                )
            }
            .then(if (scale > 1.02f) Modifier.transformable(transformState) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        val model = if (useFallback) fallbackUrl else primaryModel
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = "صفحة المصحف $pageNumber",
            contentScale = ContentScale.Fit,
            onError = {
                if (!useFallback) useFallback = true
            },
            loading = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = noorColors.emerald)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("جارٍ تحميل الصفحة $pageNumber...", color = Color(0xFF091629))
                }
            },
            error = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = Color(0xFF17652F), modifier = Modifier.size(44.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تعذر تحميل صفحة المصحف ($pageNumber). تحقق من اتصال الإنترنت أو احفظ الصفحات مسبقاً.",
                        color = Color(0xFF091629),
                        textAlign = TextAlign.Center
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}
