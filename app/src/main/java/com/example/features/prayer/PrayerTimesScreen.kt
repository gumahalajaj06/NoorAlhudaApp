package com.example.features.prayer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.data.CitiesCatalog
import com.example.core.data.CityOption
import com.example.core.domain.PrayerCalculationMethods
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors

@Composable
fun PrayerTimesScreen(
    state: PrayerUiState,
    onSelectCity: (CityOption) -> Unit,
    onRequestGps: () -> Unit,
    onSelectMethod: (Int) -> Unit,
    onSelectAsrSchool: (Int) -> Unit,
    onUpdatePreMinutes: (Int) -> Unit,
    onToggleAdhanSound: (Boolean) -> Unit,
    onTogglePrayerNotify: (String, Boolean) -> Unit,
    onLoadMonthly: () -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = اليوم, 1 = الشهري, 2 = الإعدادات
    var showCityDialog by remember { mutableStateOf(false) }

    val locLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            onRequestGps()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
    ) {
        NoorTopBar(
            title = "مواقيت الصلاة",
            subtitle = state.prefs.selectedCityName,
            onBack = onBack,
            actions = {
                IconButton(onClick = { showCityDialog = true }) {
                    Icon(Icons.Outlined.LocationCity, contentDescription = "اختيار المدينة", tint = noorColors.gold)
                }
                IconButton(onClick = {
                    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (fine || coarse) onRequestGps()
                    else locLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                }) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = "تحديد الموقع", tint = noorColors.gold)
                }
            }
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = noorColors.gold
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("اليوم") })
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    if (state.monthlyDays.isEmpty()) onLoadMonthly()
                },
                text = { Text("التقويم الشهري") }
            )
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("الطريقة والتنبيه") })
        }

        when (selectedTab) {
            0 -> {
                val summary = state.todaySummary
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(noorColors.emerald, Color(0xFF102849))
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("الصلاة القادمة", style = MaterialTheme.typography.labelMedium, color = Color(0xFFE7CE78))
                                    Text(
                                        text = "${summary?.nextPrayerName ?: "..."}  •  ${summary?.nextPrayerTime ?: "--:--"}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color(0xFFF7F5EF),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = summary?.sourceLabel ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFD6E2F0)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("المتبقي", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE7CE78))
                                    Text(
                                        text = state.countdownText,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color(0xFFF7F5EF),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    state.locationMessage?.let { msg ->
                        item {
                            Text(msg, style = MaterialTheme.typography.labelMedium, color = noorColors.gold)
                        }
                    }

                    val rows = listOf(
                        Triple("الفجر", summary?.schedule?.fajr ?: "--:--", "fajr" to state.prefs.notifyFajr),
                        Triple("الشروق", summary?.schedule?.sunrise ?: "--:--", null),
                        Triple("الظهر", summary?.schedule?.dhuhr ?: "--:--", "dhuhr" to state.prefs.notifyDhuhr),
                        Triple("العصر", summary?.schedule?.asr ?: "--:--", "asr" to state.prefs.notifyAsr),
                        Triple("المغرب", summary?.schedule?.maghrib ?: "--:--", "maghrib" to state.prefs.notifyMaghrib),
                        Triple("العشاء", summary?.schedule?.isha ?: "--:--", "isha" to state.prefs.notifyIsha)
                    )

                    items(rows) { (name, time, notifyPair) ->
                        val isNext = summary?.nextPrayerName == name
                        NoorGoldCard(
                            borderColor = if (isNext) noorColors.gold else noorColors.goldBorder
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = if (isNext) noorColors.gold else noorColors.emeraldBright
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isNext) noorColors.gold else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (notifyPair != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = { onTogglePrayerNotify(notifyPair.first, !notifyPair.second) }) {
                                            Icon(
                                                imageVector = if (notifyPair.second) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsOff,
                                                contentDescription = "تنبيه $name",
                                                tint = if (notifyPair.second) noorColors.gold else noorColors.mutedText
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                if (state.isLoadingMonth) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = noorColors.gold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.monthlyDays) { d ->
                            NoorGoldCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${d.gregorianDay} م • ${d.hijriDay} ${d.hijriMonth}", color = noorColors.gold, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MiniPrayerCell("فجر", d.fajr)
                                    MiniPrayerCell("ظهر", d.dhuhr)
                                    MiniPrayerCell("عصر", d.asr)
                                    MiniPrayerCell("مغرب", d.maghrib)
                                    MiniPrayerCell("عشاء", d.isha)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        NoorGoldCard {
                            Text("طريقة الحساب", style = MaterialTheme.typography.titleMedium, color = noorColors.gold, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            PrayerCalculationMethods.methods.forEach { method ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = state.prefs.calculationMethodId == method.id,
                                        onClick = { onSelectMethod(method.id) }
                                    )
                                    Text(method.nameArabic, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    item {
                        NoorGoldCard {
                            Text("مذهب حساب العصر", style = MaterialTheme.typography.titleMedium, color = noorColors.gold, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = state.prefs.asrSchool == 0,
                                    onClick = { onSelectAsrSchool(0) },
                                    label = { Text("الجمهور (الشافعي/المالكي/الحنبلي)") }
                                )
                                FilterChip(
                                    selected = state.prefs.asrSchool == 1,
                                    onClick = { onSelectAsrSchool(1) },
                                    label = { Text("الحنفي") }
                                )
                            }
                        }
                    }
                    item {
                        NoorGoldCard {
                            Text("التنبيهات وصوت الأذان", style = MaterialTheme.typography.titleMedium, color = noorColors.gold, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("التنبيه قبل الصلاة بـ: ${state.prefs.prePrayerAlertMinutes} دقيقة")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(0, 5, 10, 15, 20).forEach { min ->
                                    FilterChip(
                                        selected = state.prefs.prePrayerAlertMinutes == min,
                                        onClick = { onUpdatePreMinutes(min) },
                                        label = { Text("$min د") }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("تشغيل نغمة التنبيه / الأذان بموافقتك")
                                Switch(
                                    checked = state.prefs.adhanSoundEnabled,
                                    onCheckedChange = onToggleAdhanSound
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCityDialog) {
        var filter by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = { Text("اختر المدينة") },
            text = {
                Column {
                    OutlinedTextField(
                        value = filter,
                        onValueChange = { filter = it },
                        placeholder = { Text("ابحث عن مدينة...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        val filtered = CitiesCatalog.cities.filter {
                            filter.isBlank() || it.nameArabic.contains(filter) || it.countryArabic.contains(filter)
                        }
                        items(filtered, key = { it.id }) { city ->
                            TextButton(
                                onClick = {
                                    onSelectCity(city)
                                    showCityDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(city.nameArabic)
                                    Text(city.countryArabic, color = noorColors.gold, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCityDialog = false }) { Text("إغلاق") } }
        )
    }
}

@Composable
private fun MiniPrayerCell(label: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = LocalNoorColors.current.mutedText)
        Text(time, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}
