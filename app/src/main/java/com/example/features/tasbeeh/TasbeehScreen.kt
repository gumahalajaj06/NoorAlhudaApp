package com.example.features.tasbeeh

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily

@Composable
fun TasbeehScreen(
    state: TasbeehUiState,
    azkarOptions: List<String>,
    onTap: () -> Unit,
    onSelectTarget: (Int) -> Unit,
    onSelectZikr: (Int) -> Unit,
    onReset: () -> Unit,
    onToggleVibrate: () -> Unit,
    onToggleSound: () -> Unit,
    onClearHistory: () -> Unit,
    onGoHome: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    var showCustomTargetDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tasbeeh_scale"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = state.progressFraction,
        label = "tasbeeh_progress"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            NoorTopBar(
                title = "المسبحة الإلكترونية",
                subtitle = "سجل التسبيح اليومي محفوظ تلقائياً",
                actions = {
                    IconButton(onClick = onGoHome, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Outlined.Home, contentDescription = "الرئيسية", tint = noorColors.gold)
                    }
                    IconButton(onClick = { showHistorySheet = true }, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Outlined.History, contentDescription = "سجل التسبيح", tint = noorColors.gold)
                    }
                }
            )
        }

        // Zikr Selector
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(azkarOptions) { index, label ->
                    val selected = index == state.selectedZikrIndex
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectZikr(index) },
                        label = {
                            Text(
                                text = label,
                                fontFamily = LocalScriptFontFamily.current,
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
        }

        // Active Zikr Banner
        item {
            NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = azkarOptions[state.selectedZikrIndex],
                    fontFamily = LocalScriptFontFamily.current,
                    style = MaterialTheme.typography.displayMedium,
                    color = noorColors.gold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Large Interactive Circular Tasbeeh Bead Button
        item {
            Box(
                modifier = Modifier
                    .size(255.dp)
                    .scale(buttonScale)
                    .testTag("tasbeeh_tap_circle")
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = androidx.compose.material3.ripple(color = noorColors.gold),
                        onClick = onTap
                    ),
                contentAlignment = Alignment.Center
            ) {
                val trackColor = noorColors.gold.copy(alpha = 0.16f)
                val goldColor = noorColors.gold
                val emeraldColor = noorColors.emerald
                Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                emeraldColor.copy(alpha = 0.92f),
                                Color(0xFF0E203B)
                            )
                        )
                    )
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = goldColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.count.toString(),
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF7F5EF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .testTag("tasbeeh_count_text")
                            .widthIn(min = 48.dp)
                    )
                    Text(
                        text = "من ${state.target}  •  ${state.progressPercent}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = noorColors.gold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اضغط للتسبيح",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE7CE78)
                    )
                }
            }
        }

        // Stats Row: Total, Cycles, Today
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile("الإجمالي", state.totalCount.toString(), Modifier.weight(1f))
                StatTile("الدورات", state.cycles.toString(), Modifier.weight(1f))
                StatTile("تسبيح اليوم", state.todayTotal.toString(), Modifier.weight(1f))
            }
        }

        // Target Selector (33, 100, 1000, Custom)
        item {
            NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "هدف التسبيح",
                    style = MaterialTheme.typography.titleMedium,
                    color = noorColors.gold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(33, 100, 1000).forEach { preset ->
                        FilterChip(
                            selected = state.target == preset,
                            onClick = { onSelectTarget(preset) },
                            label = { Text(preset.toString()) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedButton(
                        onClick = { showCustomTargetDialog = true },
                        border = BorderStroke(1.dp, noorColors.goldBorder)
                    ) {
                        Text("مخصص", color = noorColors.gold)
                    }
                }
            }
        }

        // Controls: Vibrate, Click Sound, Reset
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.vibrateEnabled,
                    onClick = onToggleVibrate,
                    label = { Text("اهتزاز") },
                    leadingIcon = { Icon(Icons.Outlined.Vibration, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.clickSoundEnabled,
                    onClick = onToggleSound,
                    label = { Text("صوت نقرة") },
                    leadingIcon = { Icon(Icons.Outlined.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onReset,
                    border = BorderStroke(1.dp, noorColors.goldBorder),
                    modifier = Modifier.testTag("tasbeeh_reset_button")
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, tint = noorColors.gold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصفير", color = noorColors.gold)
                }
            }
        }
    }

    if (showCustomTargetDialog) {
        var customText by remember { mutableStateOf(state.target.toString()) }
        AlertDialog(
            onDismissRequest = { showCustomTargetDialog = false },
            title = { Text("تحديد هدف مخصص") },
            text = {
                OutlinedTextField(
                    value = customText,
                    onValueChange = { if (it.all(Char::isDigit)) customText = it },
                    label = { Text("العدد المطلوب") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    customText.toIntOrNull()?.let(onSelectTarget)
                    showCustomTargetDialog = false
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTargetDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showHistorySheet) {
        AlertDialog(
            onDismissRequest = { showHistorySheet = false },
            title = { Text("سجل التسبيح اليومي") },
            text = {
                if (state.recentLogs.isEmpty()) {
                    Text("لم يتم تسجيل تسبيحات بعد.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(state.recentLogs.take(30)) { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.zikrText, modifier = Modifier.weight(1f))
                                Text("${log.dateKey} (+${log.count})", color = noorColors.gold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearHistory()
                    showHistorySheet = false
                }) { Text("مسح السجل", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                Button(onClick = { showHistorySheet = false }) { Text("إغلاق") }
            }
        )
    }
}

@Composable
private fun StatTile(title: String, value: String, modifier: Modifier = Modifier) {
    val noorColors = LocalNoorColors.current
    NoorGoldCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = noorColors.mutedText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = noorColors.gold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
