package com.example.features.more

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors

private data class MoreMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun MoreScreen(
    state: MoreUiState,
    onOpenQibla: () -> Unit,
    onOpenPrayerTimes: () -> Unit,
    onOpenHijri: () -> Unit,
    onOpenDuas: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenAbout: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onGoHome: () -> Unit
) {
    val noorColors = LocalNoorColors.current

    val itemsList = listOf(
        MoreMenuItem("اتجاه القبلة", "بوصلة إسلامية دقيقة وموقع الكعبة", Icons.Filled.Explore, "more_qibla", onOpenQibla),
        MoreMenuItem("مواقيت الصلاة", "المواقيت والتقويم الشهري وطريقة الحساب", Icons.Filled.Schedule, "more_prayer", onOpenPrayerTimes),
        MoreMenuItem("التقويم الهجري", "التقويم والمناسبات وتعديل الفرق", Icons.Filled.CalendarMonth, "more_hijri", onOpenHijri),
        MoreMenuItem("الأدعية والأحاديث", "أدعية مأثورة من القرآن والسنة", Icons.Filled.VolunteerActivism, "more_duas", onOpenDuas),
        MoreMenuItem(
            "المفضلة والعلامات",
            "${state.favoriteAyahs.size + state.favoriteAzkar.size} مفضلة • ${state.bookmarks.size} علامة",
            Icons.Filled.Favorite,
            "more_favorites",
            onOpenFavorites
        ),
        MoreMenuItem(
            "إدارة التنزيلات",
            "${state.downloadedAudio.size} تلاوة • ${state.downloadedMushafPages} صفحة مصحف",
            Icons.Filled.CloudDone,
            "more_downloads",
            onOpenDownloads
        ),
        MoreMenuItem("التنبيهات والإعدادات", "إشعارات الصلاة، الخط، الصوت، اللغة", Icons.Filled.Settings, "more_settings", onOpenSettings),
        MoreMenuItem("سياسة الخصوصية", "حماية بياناتك وأذونات الموقع", Icons.Filled.Security, "more_privacy", onOpenPrivacyPolicy),
        MoreMenuItem("حول التطبيق والمطور", "نور الهدى v1.0.0 — جمعة العجاج", Icons.Filled.Info, "more_about", onOpenAbout)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
            .testTag("more_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            NoorTopBar(
                title = "المزيد والإعدادات",
                subtitle = "نور الهدى | Noor AlHuda",
                actions = {
                    IconButton(onClick = onGoHome, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(Icons.Outlined.Home, contentDescription = "الرئيسية", tint = noorColors.gold)
                    }
                }
            )
        }

        // Quick Theme Switch + Identity Banner
        item {
            NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_fg),
                        contentDescription = "شعار نور الهدى",
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, noorColors.gold, RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (state.prefs.isDarkMode) "الوضع الليلي الكحلي الفاخر" else "الوضع النهاري العاجي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تبديل المظهر الفوري للتطبيق",
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.mutedText
                        )
                    }
                    Switch(
                        checked = state.prefs.isDarkMode,
                        onCheckedChange = onToggleTheme,
                        modifier = Modifier.testTag("more_theme_switch")
                    )
                }
            }
        }

        items(itemsList, key = { it.testTag }) { item ->
            NoorGoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(item.testTag),
                onClick = item.onClick
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(noorColors.emerald.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = item.title, tint = noorColors.gold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(item.subtitle, style = MaterialTheme.typography.labelMedium, color = noorColors.mutedText)
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = noorColors.gold
                    )
                }
            }
        }
    }
}
