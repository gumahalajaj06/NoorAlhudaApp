package com.example.features.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ui.components.BasmalahOrnament
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorSectionHeader
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily

@Composable
fun HomeScreen(
    state: HomeUiState,
    onDetectLocation: () -> Unit,
    onOpenMushaf: (Int) -> Unit,
    onOpenLastSurah: (Int, Int) -> Unit,
    onOpenQuranIndex: () -> Unit,
    onOpenSurah: (Int) -> Unit,
    onOpenAzkar: () -> Unit,
    onOpenTasbeeh: () -> Unit,
    onOpenAudio: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenPrayerDetails: () -> Unit,
    onOpenHijriCalendar: () -> Unit,
    onIncrementDailyRead: () -> Unit,
    onUpdateDailyGoal: (Int) -> Unit
) {
    val noorColors = LocalNoorColors.current
    val context = LocalContext.current
    var showGoalDialog by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) onDetectLocation()
    }

    val requestLocationClick = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            onDetectLocation()
        } else {
            locationLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. ترحيب باسم التطبيق وشعار نور الهدى
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .border(1.dp, noorColors.goldBorder, RoundedCornerShape(26.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "خلفية نور الهدى",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xE0091629),
                                    Color(0xB817652F)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_fg),
                        contentDescription = "شعار نور الهدى",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.5.dp, noorColors.gold, RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مرحباً بك في نور الهدى",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFF7F5EF),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Noor AlHuda — رفيقك اليومي للقرآن والذكر والصلاة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE7CE78)
                        )
                    }
                }
            }
        }

        // 2. عبارة بسم الله الرحمن الرحيم بخط عربي جميل
        item {
            BasmalahOrnament(modifier = Modifier.padding(vertical = 2.dp))
        }

        // 3 + 4 + 5 + 6. بطاقة مواقيت الصلاة + التاريخ الهجري والميلادي + الموقع + الصلاة القادمة والوقت المتبقي
        item {
            val summary = state.prayerSummary
            val schedule = summary?.schedule
            NoorGoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_prayer_card"),
                onClick = onOpenPrayerDetails,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                // 4. التاريخ الهجري والميلادي
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${state.hijriDate.weekdayArabic}، ${state.hijriDate.formattedArabic}",
                            style = MaterialTheme.typography.titleMedium,
                            color = noorColors.gold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.hijriDate.gregorianDisplay,
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.mutedText
                        )
                    }
                    Surface(
                        onClick = onOpenHijriCalendar,
                        shape = RoundedCornerShape(50),
                        color = noorColors.emerald.copy(alpha = 0.25f),
                        border = BorderStroke(0.8.dp, noorColors.goldBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "التقويم الهجري",
                                tint = noorColors.gold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "التقويم",
                                style = MaterialTheme.typography.labelMedium,
                                color = noorColors.gold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. موقع المستخدم الحالي وحالة تحديد الموقع
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "الموقع",
                            tint = noorColors.emeraldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.locationStatusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TextButton(
                        onClick = requestLocationClick,
                        modifier = Modifier.testTag("home_gps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = "تحديد الموقع",
                            tint = noorColors.gold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تحديث الموقع", color = noorColors.gold, style = MaterialTheme.typography.labelMedium)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = noorColors.goldBorder
                )

                // 6. عرض الصلاة القادمة والوقت المتبقي لها
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(noorColors.emerald.copy(alpha = 0.85f), noorColors.emeraldBright.copy(alpha = 0.65f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الصلاة القادمة",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFE7CE78)
                        )
                        Text(
                            text = "${summary?.nextPrayerName ?: "..."}  •  ${summary?.nextPrayerTime ?: "--:--"}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFF7F5EF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "الوقت المتبقي",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE7CE78)
                        )
                        Text(
                            text = state.countdownText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color(0xFFF7F5EF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. بطاقة مواقيت الصلاة الستة
                val prayersRow = listOf(
                    "الفجر" to (schedule?.fajr ?: "--:--"),
                    "الشروق" to (schedule?.sunrise ?: "--:--"),
                    "الظهر" to (schedule?.dhuhr ?: "--:--"),
                    "العصر" to (schedule?.asr ?: "--:--"),
                    "المغرب" to (schedule?.maghrib ?: "--:--"),
                    "العشاء" to (schedule?.isha ?: "--:--")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    prayersRow.forEach { (name, time) ->
                        val isNext = summary?.nextPrayerName == name
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isNext) noorColors.gold.copy(alpha = 0.2f) else Color.Transparent
                                )
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isNext) noorColors.gold else noorColors.mutedText,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = time,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isNext) noorColors.gold else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 7. أزرار وصول سريع إلى الأقسام الستة المطلوبة
        item {
            NoorSectionHeader(title = "الوصول السريع")
            Spacer(modifier = Modifier.height(8.dp))
            val quickActions = listOf(
                QuickActionModel("مصحف القرآن", "604 صفحة", Icons.Filled.MenuBook, "quick_mushaf") {
                    onOpenMushaf(state.prefs.lastMushafPage)
                },
                QuickActionModel(
                    "آخر قراءة",
                    "سورة ${state.prefs.lastReadSurahName}",
                    Icons.Filled.Bookmark,
                    "quick_last_surah"
                ) {
                    onOpenLastSurah(state.prefs.lastReadSurah, state.prefs.lastReadAyah)
                },
                QuickActionModel("الأذكار", "حصن المسلم", Icons.Filled.AutoAwesome, "quick_azkar", onOpenAzkar),
                QuickActionModel("المسبحة", "تسبيح وذكر", Icons.Filled.Adjust, "quick_tasbeeh", onOpenTasbeeh),
                QuickActionModel("التلاوة الصوتية", "كبار القراء", Icons.Filled.Headphones, "quick_audio", onOpenAudio),
                QuickActionModel("اتجاه القبلة", "بوصلة دقيقة", Icons.Filled.Explore, "quick_qibla", onOpenQibla)
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quickActions.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            QuickActionTile(
                                item = item,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 8. قسم سور القرآن الكريم مع عدد الآيات واسم السورة ونوعها
        item {
            NoorSectionHeader(
                title = "سور القرآن الكريم",
                subtitle = "114 سورة • 6236 آية بالرسم العثماني",
                actionLabel = "عرض الكل",
                onActionClick = onOpenQuranIndex
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.featuredSurahs.take(20), key = { it.number }) { surah ->
                    NoorGoldCard(
                        modifier = Modifier
                            .width(154.dp)
                            .testTag("home_surah_card_${surah.number}"),
                        onClick = { onOpenSurah(surah.number) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(noorColors.emerald.copy(alpha = 0.28f))
                                    .border(1.dp, noorColors.goldBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = noorColors.gold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = noorColors.gold.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = surah.revelationType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = noorColors.gold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "سورة ${surah.nameArabic}",
                            fontFamily = LocalScriptFontFamily.current,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${surah.ayahCount} آية • ص ${surah.startPage}",
                            style = MaterialTheme.typography.labelMedium,
                            color = noorColors.mutedText
                        )
                    }
                }
            }
        }

        // 9. قسم متابعة القراءة اليومية ونسبة الإنجاز
        item {
            val read = state.prefs.dailyReadPagesCount
            val goal = state.prefs.dailyGoalPages.coerceAtLeast(1)
            val progress = (read.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
            val percent = (progress * 100).toInt()

            NoorSectionHeader(
                title = "متابعة القراءة اليومية",
                actionLabel = "تعديل الهدف",
                onActionClick = { showGoalDialog = true }
            )
            Spacer(modifier = Modifier.height(8.dp))
            NoorGoldCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_daily_progress_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ورد اليوم: $read من $goal صفحات",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (percent >= 100) "بارك الله فيك! أتممت وردك اليومي"
                            else "واصل القراءة للوصول إلى هدفك اليومي",
                            style = MaterialTheme.typography.bodyMedium,
                            color = noorColors.mutedText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(noorColors.emerald.copy(alpha = 0.25f))
                            .border(1.5.dp, noorColors.gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$percent%",
                            style = MaterialTheme.typography.titleMedium,
                            color = noorColors.gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = noorColors.gold,
                    trackColor = noorColors.gold.copy(alpha = 0.18f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onIncrementDailyRead,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("home_mark_page_read"),
                        colors = ButtonDefaults.buttonColors(containerColor = noorColors.emerald)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تسجيل صفحة مقروءة")
                    }
                    OutlinedButton(
                        onClick = { onOpenMushaf(state.prefs.lastMushafPage) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize(),
                        border = BorderStroke(1.dp, noorColors.gold)
                    ) {
                        Text("فتح المصحف", color = noorColors.gold)
                    }
                }
            }
        }
    }

    if (showGoalDialog) {
        var goalVal by remember { mutableIntStateOf(state.prefs.dailyGoalPages) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("الهدف اليومي للقراءة", textAlign = TextAlign.Start) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("اختر عدد الصفحات اليومية المستهدفة:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(2, 4, 10, 20).forEach { preset ->
                            FilterChip(
                                selected = goalVal == preset,
                                onClick = { goalVal = preset },
                                label = { Text("$preset ص") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateDailyGoal(goalVal)
                    showGoalDialog = false
                }) { Text("حفظ") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

private data class QuickActionModel(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionTile(
    item: QuickActionModel,
    modifier: Modifier = Modifier
) {
    val noorColors = LocalNoorColors.current
    NoorGoldCard(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(item.testTag),
        onClick = item.onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(noorColors.emerald.copy(alpha = 0.25f))
                    .border(1.dp, noorColors.goldBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = noorColors.gold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = noorColors.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
