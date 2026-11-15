package com.example.features.azkar

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.core.data.ZikrCategory
import com.example.core.data.ZikrItem
import com.example.ui.components.ArabicFontLivePreviewCard
import com.example.ui.components.ArabicFontOptionsList
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarScreen(
    state: AzkarUiState,
    selectedFontId: String,
    onSelectFont: (String) -> Unit,
    onSelectCategory: (ZikrCategory) -> Unit,
    onDecrementZikr: (ZikrItem) -> Unit,
    onResetZikr: (ZikrItem) -> Unit,
    onResetCategory: () -> Unit,
    onToggleFavorite: (ZikrItem) -> Unit,
    onSyncRemote: () -> Unit,
    onClearSyncMessage: () -> Unit,
    onGoHome: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    val scriptFontFamily = LocalScriptFontFamily.current
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFontSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.syncMessage) {
        state.syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSyncMessage()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 680.dp)
        ) {
            NoorTopBar(
                title = "الأذكار اليومية",
                subtitle = "حصن المسلم — ${state.categories.size} تصنيفاً",
                actions = {
                    IconButton(onClick = onGoHome, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Outlined.Home, contentDescription = "الرئيسية", tint = noorColors.gold)
                    }
                    IconButton(
                        onClick = { showFontSheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("azkar_font_picker_button")
                    ) {
                        Icon(Icons.Outlined.TextFields, contentDescription = "تخصيص الخط", tint = noorColors.gold)
                    }
                    IconButton(
                        onClick = onSyncRemote,
                        enabled = !state.isSyncingRemote,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        if (state.isSyncingRemote) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = noorColors.gold)
                        } else {
                            Icon(Icons.Outlined.Sync, contentDescription = "تحديث الأذكار", tint = noorColors.gold)
                        }
                    }
                    IconButton(onClick = onResetCategory, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "إعادة ضبط التصنيف", tint = noorColors.gold)
                    }
                }
            )

            // 12 Categories Horizontal Chip Bar
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories, key = { it.id }) { cat ->
                    val selected = cat.id == state.selectedCategory.id
                    FilterChip(
                        selected = selected,
                        onClick = {
                            onSelectCategory(cat)
                            scope.launch { listState.scrollToItem(0) }
                        },
                        label = {
                            Text(
                                text = cat.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = noorColors.emerald,
                            selectedLabelColor = Color(0xFFF7F5EF)
                        )
                    )
                }
            }

            // Category subtitle banner
            Text(
                text = state.selectedCategory.subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = noorColors.gold,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(state.itemsInCategory, key = { _, item -> item.id }) { index, item ->
                    val remaining = state.remainingCounts[item.id] ?: item.repeatCount
                    val completed = remaining == 0
                    val isFav = state.favoriteIds.contains(item.id)
                    val progress = if (item.repeatCount > 0) {
                        ((item.repeatCount - remaining).toFloat() / item.repeatCount.toFloat()).coerceIn(0f, 1f)
                    } else 1f
                    val cardBorder by animateColorAsState(
                        targetValue = if (completed) noorColors.emeraldBright else noorColors.goldBorder,
                        label = "zikr_border"
                    )

                    NoorGoldCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("zikr_card_${item.id}"),
                        borderColor = cardBorder
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = noorColors.gold.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = item.grade,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = noorColors.gold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Row {
                                IconButton(onClick = { onResetZikr(item) }) {
                                    Icon(Icons.Outlined.Replay, contentDescription = "إعادة", tint = noorColors.mutedText)
                                }
                                IconButton(onClick = { onToggleFavorite(item) }) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "مفضلة",
                                        tint = if (isFav) Color(0xFFE57373) else noorColors.mutedText
                                    )
                                }
                                IconButton(onClick = {
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${item.text}\n[${item.reference}] — تطبيق نور الهدى")
                                    }
                                    context.startActivity(Intent.createChooser(share, "مشاركة الذكر"))
                                }) {
                                    Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = noorColors.mutedText)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.text,
                            fontFamily = scriptFontFamily,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.25f,
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "المصدر: ${item.reference}",
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.mutedText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (completed) noorColors.emeraldBright else noorColors.gold,
                            trackColor = noorColors.gold.copy(alpha = 0.16f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    onDecrementZikr(item)
                                    if (remaining == 1 && index < state.itemsInCategory.lastIndex) {
                                        scope.launch { listState.animateScrollToItem(index + 1) }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("zikr_tap_${item.id}"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (completed) noorColors.emeraldBright else noorColors.emerald
                                )
                            ) {
                                Text(
                                    text = if (completed) "تم إكمال التكرار ✓"
                                    else "تسبيح / تكرار  •  المتبقي $remaining من ${item.repeatCount}",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (index < state.itemsInCategory.lastIndex) {
                                OutlinedButton(
                                    onClick = { scope.launch { listState.animateScrollToItem(index + 1) } },
                                    border = BorderStroke(1.dp, noorColors.goldBorder)
                                ) {
                                    Text("التالي", color = noorColors.gold)
                                }
                            }
                        }
                    }
                }
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
                        text = "اختيار خط الأذكار والقرآن",
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
                ArabicFontLivePreviewCard(selectedFontId = selectedFontId)
                ArabicFontOptionsList(
                    selectedFontId = selectedFontId,
                    onSelectFont = onSelectFont,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
