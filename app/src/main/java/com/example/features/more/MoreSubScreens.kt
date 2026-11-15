package com.example.features.more

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.core.database.DownloadedAudioEntity
import com.example.ui.components.ArabicFontSelectorSection
import com.example.ui.components.NoorGoldCard
import com.example.ui.components.NoorSectionHeader
import com.example.ui.components.NoorTopBar
import com.example.ui.theme.LocalNoorColors
import com.example.ui.theme.LocalScriptFontFamily

@Composable
fun FavoritesScreen(
    state: MoreUiState,
    onOpenAyah: (Int, Int) -> Unit,
    onOpenMushafPage: (Int) -> Unit,
    onRemoveAyah: (String) -> Unit,
    onRemoveZikr: (String) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
        NoorTopBar(title = "المفضلة والعلامات", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.bookmarks.isEmpty() && state.favoriteAyahs.isEmpty() && state.favoriteAzkar.isEmpty()) {
                item {
                    NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "لا توجد عناصر محفوظة في المفضلة أو العلامات المرجعية بعد.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(20.dp)
                        )
                    }
                }
            }

            if (state.bookmarks.isNotEmpty()) {
                item { NoorSectionHeader(title = "العلامات المرجعية (${state.bookmarks.size})") }
                items(state.bookmarks, key = { it.id }) { bm ->
                    NoorGoldCard(
                        onClick = {
                            if (bm.type == "MUSHAF") onOpenMushafPage(bm.pageNumber)
                            else onOpenAyah(bm.surahNumber, bm.ayahNumber)
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bm.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(bm.subtitle, style = MaterialTheme.typography.labelMedium, color = noorColors.mutedText)
                            }
                            IconButton(onClick = { onRemoveBookmark(bm.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            if (state.favoriteAyahs.isNotEmpty()) {
                item { NoorSectionHeader(title = "الآيات المفضلة (${state.favoriteAyahs.size})") }
                items(state.favoriteAyahs, key = { it.key }) { ayah ->
                    NoorGoldCard(onClick = { onOpenAyah(ayah.surahNumber, ayah.ayahNumber) }) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("سورة ${ayah.surahName} • آية ${ayah.ayahNumber}", color = noorColors.gold, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onRemoveAyah(ayah.key) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text(ayah.textUthmani, fontFamily = LocalScriptFontFamily.current, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            if (state.favoriteAzkar.isNotEmpty()) {
                item { NoorSectionHeader(title = "الأذكار والأدعية المفضلة (${state.favoriteAzkar.size})") }
                items(state.favoriteAzkar, key = { it.id }) { item ->
                    NoorGoldCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.categoryTitle, color = noorColors.gold, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onRemoveZikr(item.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Text(item.text, fontFamily = LocalScriptFontFamily.current, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.reference, style = MaterialTheme.typography.labelSmall, color = noorColors.mutedText)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    state: MoreUiState,
    audioVolume: Float,
    onToggleDarkMode: (Boolean) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onArabicFontChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onToggleNotification: (String, Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onOpenPrayerSettings: () -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    val context = LocalContext.current
    var notifPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifPermissionGranted = granted
    }

    Column(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
        NoorTopBar(title = "الإعدادات والتنبيهات", onBack = onBack)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NoorGoldCard {
                    Text("المظهر وحجم الخط", style = MaterialTheme.typography.titleMedium, color = noorColors.gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("الوضع الداكن (الكحلي الفاخر)")
                        Switch(checked = state.prefs.isDarkMode, onCheckedChange = onToggleDarkMode)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("حجم خط القرآن الكريم: ${state.prefs.quranFontSize.toInt()} sp")
                    Slider(
                        value = state.prefs.quranFontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 18f..36f
                    )
                }
            }

            item {
                NoorGoldCard(modifier = Modifier.testTag("settings_font_card")) {
                    Text(
                        text = "الخط العربي للقرآن والأذكار",
                        style = MaterialTheme.typography.titleMedium,
                        color = noorColors.gold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اختر نوع الخط المفضل لقراءة الآيات الكريمة والأذكار والأدعية:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = noorColors.mutedText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ArabicFontSelectorSection(
                        selectedFontId = state.prefs.selectedArabicFont,
                        previewFontSizeSp = state.prefs.quranFontSize.coerceAtMost(28f),
                        onSelectFont = onArabicFontChange
                    )
                }
            }

            item {
                NoorGoldCard {
                    Text("إشعارات وتنبيهات التطبيق", style = MaterialTheme.typography.titleMedium, color = noorColors.gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!notifPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        OutlinedButton(
                            onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تفعيل إذن الإشعارات في Android", color = noorColors.gold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    SettingSwitchRow("تذكير أذكار الصباح (6:00 ص)", state.prefs.notifyMorningAzkar) {
                        onToggleNotification("morning_azkar", it)
                    }
                    SettingSwitchRow("تذكير أذكار المساء (5:00 م)", state.prefs.notifyEveningAzkar) {
                        onToggleNotification("evening_azkar", it)
                    }
                    SettingSwitchRow("تذكير الورد القرآني اليومي", state.prefs.notifyDailyReading) {
                        onToggleNotification("daily_reading", it)
                    }
                    TextButton(onClick = onOpenPrayerSettings) {
                        Text("تخصيص تنبيهات الصلوات الخمس وصوت الأذان ←", color = noorColors.gold)
                    }
                }
            }

            item {
                NoorGoldCard {
                    Text("إعدادات الصوت", style = MaterialTheme.typography.titleMedium, color = noorColors.gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("مستوى صوت التلاوة: ${(audioVolume * 100).toInt()}%")
                    Slider(value = audioVolume, onValueChange = onVolumeChange, valueRange = 0f..1f)
                }
            }

            item {
                NoorGoldCard {
                    Text("اللغة (تجهيز التوسع مستقبلاً)", style = MaterialTheme.typography.titleMedium, color = noorColors.gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.prefs.appLanguage == "ar",
                            onClick = { onLanguageChange("ar") },
                            label = { Text("العربية (الافتراضية)") }
                        )
                        FilterChip(
                            selected = state.prefs.appLanguage == "en",
                            onClick = { onLanguageChange("en") },
                            label = { Text("English (قريباً)") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun DownloadsScreen(
    state: MoreUiState,
    onDeleteAudio: (DownloadedAudioEntity) -> Unit,
    onClearMushafPages: () -> Unit,
    onBack: () -> Unit
) {
    val noorColors = LocalNoorColors.current
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
        NoorTopBar(title = "إدارة التنزيلات", subtitle = "الملفات المحفوظة للعمل دون إنترنت", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NoorGoldCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("صفحات المصحف المدني المحفوظة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${state.downloadedMushafPages} من 604 صفحة محفوظة محلياً", color = noorColors.mutedText)
                        }
                        if (state.downloadedMushafPages > 0) {
                            OutlinedButton(onClick = onClearMushafPages) {
                                Text("مسح", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            item {
                NoorSectionHeader(title = "التلاوات الصوتية المنزّلة (${state.downloadedAudio.size})")
            }

            if (state.downloadedAudio.isEmpty()) {
                item {
                    NoorGoldCard {
                        Text(
                            text = "لم يتم تنزيل أي تلاوة صوتية بعد. يمكنك تنزيل السور من قسم التلاوة الصوتية.",
                            color = noorColors.mutedText
                        )
                    }
                }
            } else {
                items(state.downloadedAudio, key = { it.id }) { item ->
                    val mb = "%.1f MB".format(item.fileSizeBytes / (1024f * 1024f))
                    NoorGoldCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("سورة ${item.surahName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${item.reciterName} • $mb", style = MaterialTheme.typography.labelMedium, color = noorColors.mutedText)
                            }
                            IconButton(onClick = { onDeleteAudio(item) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "حذف التلاوة", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val noorColors = LocalNoorColors.current
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
        NoorTopBar(title = "سياسة الخصوصية", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NoorGoldCard {
                    Text("التزامنا بالخصوصية", style = MaterialTheme.typography.titleLarge, color = noorColors.gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """
                        • تطبيق نور الهدى لا يجمع أو يبيع أو يشارك أي بيانات شخصية للمستخدم مع أطراف ثالثة.
                        • استخدام الموقع الجغرافي (GPS): يُطلب فقط بموافقتك الصريحة لحساب مواقيت الصلاة بدقة وتحديد اتجاه القبلة لمدينتك، ويمكنك في أي وقت اختيار المدينة يدوياً دون تفعيل الموقع.
                        • التخزين المحلي: يتم حفظ آخر موضع قراءة، المفضلة، سجل المسبحة، والتلاوات المنزّلة محلياً على جهازك فقط.
                        • الإشعارات: لا يتم تشغيل الأذان أو أي تنبيه إلا بعد تفعيله من قبلك.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val noorColors = LocalNoorColors.current
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 680.dp)) {
        NoorTopBar(title = "حول التطبيق والمطور", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon_fg),
                            contentDescription = "شعار نور الهدى",
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .border(2.dp, noorColors.gold, RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "نور الهدى | Noor AlHuda",
                            fontFamily = LocalScriptFontFamily.current,
                            style = MaterialTheme.typography.displayMedium,
                            color = noorColors.gold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الإصدار 1.0.0  •  سنة الإصدار: 2026",
                            style = MaterialTheme.typography.labelLarge,
                            color = noorColors.mutedText
                        )
                    }
                }
            }

            item {
                NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                    Text("معلومات المطور", style = MaterialTheme.typography.titleLarge, color = noorColors.gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    InfoRow("اسم المطور", "جمعة العجاج")
                    InfoRow("Developer", "Gumah Alajaj")
                    InfoRow("البريد الإلكتروني", "gumahofficial@gmail.com")
                    InfoRow("إنستغرام", "@gumah.h")
                    InfoRow("تلغرام", "@gumah_h")

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:gumahofficial@gmail.com"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = noorColors.gold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("مراسلة", color = noorColors.gold)
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/gumah_h"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Send, contentDescription = null, tint = noorColors.gold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تلغرام", color = noorColors.gold)
                        }
                    }
                }
            }

            item {
                NoorGoldCard(modifier = Modifier.fillMaxWidth()) {
                    Text("مصادر البيانات والتراخيص", style = MaterialTheme.typography.titleLarge, color = noorColors.gold, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = """
                        • نصوص القرآن الكريم بالرسم العثماني والتفسير الميسّر:
                          Al-Quran Cloud API (api.alquran.cloud) و The-Quran-Project/Quran-API (ترخيص MIT).
                        • صفحات المصحف المدني (604 صفحة):
                          صور مصحف المدينة المنورة (GovarJabbar/Quran-PNG و QuranHub/quran-pages-images — Public Domain / The Unlicense).
                        • الأذكار اليومية (حصن المسلم):
                          YousefAsalya/Islamic-Pro-azkar-API ومصادر السنة الصحيحة الموثقة.
                        • مواقيت الصلاة والتقويم الهجري:
                          Aladhan API (api.aladhan.com) مع محرك حساب فلكي محلي.
                        • التلاوات الصوتية:
                          MP3Quran API v3 (mp3quran.net) وشبكة Islamic Network CDN.
                        • الخطوط: Amiri و Noto Naskh Arabic و Cairo (SIL Open Font License 1.1).
                        • المكتبات: Jetpack Compose, Material 3, Room, DataStore, Media3 ExoPlayer, Coil, Retrofit, OkHttp, Moshi.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "© 2026 جمعة العجاج (Gumah Alajaj) — جميع الحقوق محفوظة لتطبيق نور الهدى.",
                        style = MaterialTheme.typography.labelMedium,
                        color = noorColors.gold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = LocalNoorColors.current.mutedText)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
