package com.example.features.duas

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.data.DuaCategory
import com.example.core.data.DuaItem
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily

@Composable
fun DuasScreen(
    state: DuasUiState,
    onSelectCategory: (DuaCategory) -> Unit,
    onToggleFavorite: (DuaItem) -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
    ) {
        NoorTopBar(
            title = "الأدعية المأثورة",
            subtitle = "من القرآن الكريم وصحيح السنة النبوية",
            onBack = onBack
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.categories, key = { it.id }) { cat ->
                FilterChip(
                    selected = cat.id == state.selectedCategory.id,
                    onClick = { onSelectCategory(cat) },
                    label = { Text(cat.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = noorColors.emerald,
                        selectedLabelColor = Color(0xFFF7F5EF)
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.items, key = { it.id }) { dua ->
                val isFav = state.favoriteIds.contains(dua.id)
                NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dua.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = noorColors.gold,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = noorColors.gold.copy(alpha = 0.14f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = dua.grade,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = noorColors.gold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = { onToggleFavorite(dua) }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "مفضلة",
                                    tint = if (isFav) Color(0xFFE57373) else noorColors.mutedText
                                )
                            }
                            IconButton(onClick = {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "${dua.title}\n${dua.text}\n[${dua.reference}] — تطبيق نور الهدى")
                                }
                                context.startActivity(Intent.createChooser(share, "مشاركة الدعاء"))
                            }) {
                                Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = noorColors.mutedText)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = dua.text,
                        fontFamily = LocalScriptFontFamily.current,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.titleLarge.lineHeight * 1.25f
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "المصدر: ${dua.reference}",
                        style = MaterialTheme.typography.labelMedium,
                        color = noorColors.mutedText
                    )
                }
            }
        }
    }
}
