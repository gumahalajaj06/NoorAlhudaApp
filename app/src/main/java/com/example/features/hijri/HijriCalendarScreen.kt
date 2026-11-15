package com.example.features.hijri

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.domain.HijriCalendarHelper
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorSectionHeader
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import java.util.Calendar

@Composable
fun HijriCalendarScreen(
    hijriOffsetDays: Int,
    onOffsetChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    val initialCal = remember { Calendar.getInstance() }
    var displayedYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var displayedMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH) + 1) }

    val todayHijri = remember(hijriOffsetDays) { HijriCalendarHelper.today(hijriOffsetDays) }
    val daysInMonth = remember(displayedYear, displayedMonth) {
        Calendar.getInstance().apply { set(displayedYear, displayedMonth - 1, 1) }
            .getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val firstDayOfWeekOffset = remember(displayedYear, displayedMonth) {
        val c = Calendar.getInstance().apply { set(displayedYear, displayedMonth - 1, 1) }
        (c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY) // 0=Sunday .. 6=Saturday
    }

    val monthDays = remember(displayedYear, displayedMonth, hijriOffsetDays) {
        (1..daysInMonth).map { day ->
            day to HijriCalendarHelper.fromGregorian(displayedYear, displayedMonth, day, hijriOffsetDays)
        }
    }

    val isCurrentMonth = displayedYear == initialCal.get(Calendar.YEAR) &&
        displayedMonth == (initialCal.get(Calendar.MONTH) + 1)
    val todayDayOfMonth = initialCal.get(Calendar.DAY_OF_MONTH)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            NoorTopBar(
                title = "التقويم الهجري والميلادي",
                subtitle = todayHijri.formattedArabic,
                onBack = onBack
            )
        }

        // Today Summary + Offset Adjustment
        item {
            NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${todayHijri.weekdayArabic}، ${todayHijri.formattedArabic}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = noorColors.gold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = todayHijri.gregorianDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = noorColors.mutedText
                )
                todayHijri.occasion?.let { occ ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = noorColors.emerald.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "مناسبة اليوم: $occ",
                            style = MaterialTheme.typography.labelLarge,
                            color = noorColors.gold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تعديل فرق التاريخ الهجري:", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-2, -1, 0, 1, 2).forEach { off ->
                            FilterChip(
                                selected = hijriOffsetDays == off,
                                onClick = { onOffsetChange(off) },
                                label = { Text(if (off > 0) "+$off" else "$off") }
                            )
                        }
                    }
                }
            }
        }

        // Monthly Calendar Grid Card
        item {
            NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (displayedMonth == 1) {
                            displayedMonth = 12
                            displayedYear -= 1
                        } else displayedMonth -= 1
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "الشهر السابق", tint = noorColors.gold)
                    }

                    val midHijri = monthDays.getOrNull(daysInMonth / 2)?.second
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${HijriCalendarHelper.gregorianMonthsArabic[displayedMonth - 1]} $displayedYear م",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (midHijri != null) {
                            Text(
                                text = "${midHijri.monthNameArabic} ${midHijri.year} هـ",
                                style = MaterialTheme.typography.labelMedium,
                                color = noorColors.gold
                            )
                        }
                    }

                    IconButton(onClick = {
                        if (displayedMonth == 12) {
                            displayedMonth = 1
                            displayedYear += 1
                        } else displayedMonth += 1
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "الشهر التالي", tint = noorColors.gold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.labelSmall,
                            color = noorColors.gold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val cells = List(firstDayOfWeekOffset) { null } + monthDays
                cells.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        for (i in 0 until 7) {
                            val pair = week.getOrNull(i)
                            if (pair == null) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val (gDay, hijri) = pair
                                val isToday = isCurrentMonth && gDay == todayDayOfMonth
                                val hasOccasion = hijri.occasion != null
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isToday -> noorColors.emerald
                                                hasOccasion -> noorColors.gold.copy(alpha = 0.16f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 1.dp else 0.dp,
                                            color = if (isToday) noorColors.gold else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = hijri.day.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isToday) Color(0xFFF7F5EF) else noorColors.gold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$gDay",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isToday) Color(0xFFE7CE78) else noorColors.mutedText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Islamic Occasions List
        item {
            NoorSectionHeader(title = "المناسبات الإسلامية في العام الهجري")
        }

        items(HijriCalendarHelper.occasions) { occ ->
            val monthName = HijriCalendarHelper.hijriMonths[(occ.hijriMonth - 1).coerceIn(0, 11)]
            NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(noorColors.emerald.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Event, contentDescription = null, tint = noorColors.gold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(occ.titleArabic, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(occ.descriptionArabic, style = MaterialTheme.typography.bodyMedium, color = noorColors.mutedText)
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.8.dp, noorColors.goldBorder),
                        color = noorColors.gold.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "${occ.hijriDay} $monthName",
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.gold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
