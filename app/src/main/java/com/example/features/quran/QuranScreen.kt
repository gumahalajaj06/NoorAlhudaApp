package com.example.features.quran

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily

@Composable
fun QuranScreen(
    state: QuranIndexUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onOpenSurah: (Int, Int) -> Unit,
    onOpenMushaf: () -> Unit,
    onGoHome: () -> Unit
) {
    val noorColors = LocalNoorColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
    ) {
        NoorTopBar(
            title = "القرآن الكريم",
            subtitle = "114 سورة • 6236 آية بالرسم العثماني",
            actions = {
                IconButton(onClick = onGoHome, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(Icons.Outlined.Home, contentDescription = "الرئيسية", tint = noorColors.gold)
                }
                FilledTonalButton(
                    onClick = onOpenMushaf,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = noorColors.emerald.copy(alpha = 0.25f),
                        contentColor = noorColors.gold
                    )
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("المصحف")
                }
            }
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_search_input"),
                placeholder = { Text("ابحث عن سورة أو رقمها أو كلمة في الآيات...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "بحث", tint = noorColors.gold) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("الكل", "مكية", "مدنية").forEach { filter ->
                    FilterChip(
                        selected = state.filterType == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    onClick = { onOpenSurah(state.lastReadSurah, state.lastReadAyah) },
                    shape = RoundedCornerShape(50),
                    color = noorColors.gold.copy(alpha = 0.16f),
                    border = BorderStroke(0.8.dp, noorColors.goldBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = noorColors.gold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.lastReadSurahName}: ${state.lastReadAyah}",
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.gold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (state.isSearchingAyahs) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = noorColors.gold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جارٍ البحث في آيات القرآن الكريم...")
                    }
                }
            }

            if (state.ayahSearchHits.isNotEmpty()) {
                item {
                    Text(
                        text = "نتائج البحث في الآيات (${state.ayahSearchHits.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = noorColors.gold
                    )
                }
                items(state.ayahSearchHits) { hit ->
                    NoorGoldCard(
                        onClick = { onOpenSurah(hit.surahNumber, hit.ayahNumber) }
                    ) {
                        Text(
                            text = "سورة ${hit.surahName} • آية ${hit.ayahNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            color = noorColors.gold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hit.text,
                            fontFamily = LocalScriptFontFamily.current,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            items(state.surahs, key = { it.number }) { surah ->
                NoorGoldCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("surah_row_${surah.number}"),
                    onClick = { onOpenSurah(surah.number, 1) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(noorColors.emerald.copy(alpha = 0.25f))
                                .border(1.2.dp, noorColors.goldBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = surah.number.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = noorColors.gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "سورة ${surah.nameArabic}",
                                fontFamily = LocalScriptFontFamily.current,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${surah.revelationType} • ${surah.ayahCount} آية • تبدأ ص ${surah.startPage}",
                                style = MaterialTheme.typography.labelMedium,
                                color = noorColors.mutedText
                            )
                        }
                        Text(
                            text = surah.nameEnglish,
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.gold
                        )
                    }
                }
            }
        }
    }
}
