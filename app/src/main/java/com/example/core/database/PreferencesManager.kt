package com.example.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "noor_alhuda_prefs")

data class UserPreferences(
    val isDarkMode: Boolean = true,
    val quranFontSize: Float = 25f,
    val selectedArabicFont: String = "amiri",
    val appLanguage: String = "ar",
    val lastReadSurah: Int = 1,
    val lastReadAyah: Int = 1,
    val lastReadSurahName: String = "الفاتحة",
    val lastMushafPage: Int = 1,
    val dailyGoalPages: Int = 4,
    val dailyReadPagesCount: Int = 0,
    val dailyReadDate: String = "",
    val lastReciterId: String = "alafasy",
    val lastAudioSurah: Int = 1,
    val tasbeehCount: Int = 0,
    val tasbeehTarget: Int = 33,
    val tasbeehTotal: Long = 0L,
    val tasbeehCycles: Int = 0,
    val tasbeehZikrIndex: Int = 0,
    val tasbeehVibrate: Boolean = true,
    val tasbeehClickSound: Boolean = false,
    val selectedCityName: String = "مكة المكرمة",
    val selectedCountryEnglish: String = "Saudi Arabia",
    val selectedCityEnglish: String = "Makkah",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val useGpsLocation: Boolean = false,
    val calculationMethodId: Int = 4,
    val asrSchool: Int = 0,
    val hijriOffsetDays: Int = 0,
    val prePrayerAlertMinutes: Int = 10,
    val adhanSoundEnabled: Boolean = false,
    val notifyFajr: Boolean = true,
    val notifyDhuhr: Boolean = true,
    val notifyAsr: Boolean = true,
    val notifyMaghrib: Boolean = true,
    val notifyIsha: Boolean = true,
    val notifyMorningAzkar: Boolean = true,
    val notifyEveningAzkar: Boolean = true,
    val notifyDailyReading: Boolean = false
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val QURAN_FONT_SIZE = floatPreferencesKey("quran_font_size")
        val SELECTED_ARABIC_FONT = stringPreferencesKey("selected_arabic_font")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val LAST_READ_SURAH = intPreferencesKey("last_read_surah")
        val LAST_READ_AYAH = intPreferencesKey("last_read_ayah")
        val LAST_READ_SURAH_NAME = stringPreferencesKey("last_read_surah_name")
        val LAST_MUSHAF_PAGE = intPreferencesKey("last_mushaf_page")
        val DAILY_GOAL_PAGES = intPreferencesKey("daily_goal_pages")
        val DAILY_READ_PAGES_COUNT = intPreferencesKey("daily_read_pages_count")
        val DAILY_READ_DATE = stringPreferencesKey("daily_read_date")
        val LAST_RECITER_ID = stringPreferencesKey("last_reciter_id")
        val LAST_AUDIO_SURAH = intPreferencesKey("last_audio_surah")
        val TASBEEH_COUNT = intPreferencesKey("tasbeeh_count")
        val TASBEEH_TARGET = intPreferencesKey("tasbeeh_target")
        val TASBEEH_TOTAL = longPreferencesKey("tasbeeh_total")
        val TASBEEH_CYCLES = intPreferencesKey("tasbeeh_cycles")
        val TASBEEH_ZIKR_INDEX = intPreferencesKey("tasbeeh_zikr_index")
        val TASBEEH_VIBRATE = booleanPreferencesKey("tasbeeh_vibrate")
        val TASBEEH_SOUND = booleanPreferencesKey("tasbeeh_sound")
        val CITY_NAME = stringPreferencesKey("city_name")
        val COUNTRY_EN = stringPreferencesKey("country_en")
        val CITY_EN = stringPreferencesKey("city_en")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val CALC_METHOD = intPreferencesKey("calc_method")
        val ASR_SCHOOL = intPreferencesKey("asr_school")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val PRE_PRAYER_MIN = intPreferencesKey("pre_prayer_min")
        val ADHAN_SOUND = booleanPreferencesKey("adhan_sound")
        val NOTIFY_FAJR = booleanPreferencesKey("notify_fajr")
        val NOTIFY_DHUHR = booleanPreferencesKey("notify_dhuhr")
        val NOTIFY_ASR = booleanPreferencesKey("notify_asr")
        val NOTIFY_MAGHRIB = booleanPreferencesKey("notify_maghrib")
        val NOTIFY_ISHA = booleanPreferencesKey("notify_isha")
        val NOTIFY_MORNING_AZKAR = booleanPreferencesKey("notify_morning_azkar")
        val NOTIFY_EVENING_AZKAR = booleanPreferencesKey("notify_evening_azkar")
        val NOTIFY_DAILY_READING = booleanPreferencesKey("notify_daily_reading")
    }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val today = todayKey()
        val storedDate = prefs[Keys.DAILY_READ_DATE] ?: ""
        val dailyCount = if (storedDate == today) (prefs[Keys.DAILY_READ_PAGES_COUNT] ?: 0) else 0
        UserPreferences(
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
            quranFontSize = prefs[Keys.QURAN_FONT_SIZE] ?: 25f,
            selectedArabicFont = prefs[Keys.SELECTED_ARABIC_FONT] ?: "amiri",
            appLanguage = prefs[Keys.APP_LANGUAGE] ?: "ar",
            lastReadSurah = prefs[Keys.LAST_READ_SURAH] ?: 1,
            lastReadAyah = prefs[Keys.LAST_READ_AYAH] ?: 1,
            lastReadSurahName = prefs[Keys.LAST_READ_SURAH_NAME] ?: "الفاتحة",
            lastMushafPage = prefs[Keys.LAST_MUSHAF_PAGE] ?: 1,
            dailyGoalPages = prefs[Keys.DAILY_GOAL_PAGES] ?: 4,
            dailyReadPagesCount = dailyCount,
            dailyReadDate = today,
            lastReciterId = prefs[Keys.LAST_RECITER_ID] ?: "alafasy",
            lastAudioSurah = prefs[Keys.LAST_AUDIO_SURAH] ?: 1,
            tasbeehCount = prefs[Keys.TASBEEH_COUNT] ?: 0,
            tasbeehTarget = prefs[Keys.TASBEEH_TARGET] ?: 33,
            tasbeehTotal = prefs[Keys.TASBEEH_TOTAL] ?: 0L,
            tasbeehCycles = prefs[Keys.TASBEEH_CYCLES] ?: 0,
            tasbeehZikrIndex = prefs[Keys.TASBEEH_ZIKR_INDEX] ?: 0,
            tasbeehVibrate = prefs[Keys.TASBEEH_VIBRATE] ?: true,
            tasbeehClickSound = prefs[Keys.TASBEEH_SOUND] ?: false,
            selectedCityName = prefs[Keys.CITY_NAME] ?: "مكة المكرمة",
            selectedCountryEnglish = prefs[Keys.COUNTRY_EN] ?: "Saudi Arabia",
            selectedCityEnglish = prefs[Keys.CITY_EN] ?: "Makkah",
            latitude = prefs[Keys.LATITUDE] ?: 21.4225,
            longitude = prefs[Keys.LONGITUDE] ?: 39.8262,
            useGpsLocation = prefs[Keys.USE_GPS] ?: false,
            calculationMethodId = prefs[Keys.CALC_METHOD] ?: 4,
            asrSchool = prefs[Keys.ASR_SCHOOL] ?: 0,
            hijriOffsetDays = prefs[Keys.HIJRI_OFFSET] ?: 0,
            prePrayerAlertMinutes = prefs[Keys.PRE_PRAYER_MIN] ?: 10,
            adhanSoundEnabled = prefs[Keys.ADHAN_SOUND] ?: false,
            notifyFajr = prefs[Keys.NOTIFY_FAJR] ?: true,
            notifyDhuhr = prefs[Keys.NOTIFY_DHUHR] ?: true,
            notifyAsr = prefs[Keys.NOTIFY_ASR] ?: true,
            notifyMaghrib = prefs[Keys.NOTIFY_MAGHRIB] ?: true,
            notifyIsha = prefs[Keys.NOTIFY_ISHA] ?: true,
            notifyMorningAzkar = prefs[Keys.NOTIFY_MORNING_AZKAR] ?: true,
            notifyEveningAzkar = prefs[Keys.NOTIFY_EVENING_AZKAR] ?: true,
            notifyDailyReading = prefs[Keys.NOTIFY_DAILY_READING] ?: false
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_MODE] = enabled }
    }

    suspend fun setQuranFontSize(size: Float) {
        context.dataStore.edit { it[Keys.QURAN_FONT_SIZE] = size.coerceIn(18f, 38f) }
    }

    suspend fun setSelectedArabicFont(fontId: String) {
        context.dataStore.edit { it[Keys.SELECTED_ARABIC_FONT] = fontId }
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { it[Keys.APP_LANGUAGE] = lang }
    }

    suspend fun saveLastReadQuran(surah: Int, ayah: Int, surahName: String) {
        context.dataStore.edit {
            it[Keys.LAST_READ_SURAH] = surah
            it[Keys.LAST_READ_AYAH] = ayah
            it[Keys.LAST_READ_SURAH_NAME] = surahName
        }
    }

    suspend fun saveLastMushafPage(page: Int) {
        val today = todayKey()
        context.dataStore.edit { prefs ->
            val prevPage = prefs[Keys.LAST_MUSHAF_PAGE] ?: 1
            prefs[Keys.LAST_MUSHAF_PAGE] = page
            val storedDate = prefs[Keys.DAILY_READ_DATE] ?: ""
            val currentCount = if (storedDate == today) (prefs[Keys.DAILY_READ_PAGES_COUNT] ?: 0) else 0
            if (page != prevPage) {
                prefs[Keys.DAILY_READ_DATE] = today
                prefs[Keys.DAILY_READ_PAGES_COUNT] = (currentCount + 1).coerceAtMost(604)
            }
        }
    }

    suspend fun incrementDailyReadPages(delta: Int = 1) {
        val today = todayKey()
        context.dataStore.edit { prefs ->
            val storedDate = prefs[Keys.DAILY_READ_DATE] ?: ""
            val currentCount = if (storedDate == today) (prefs[Keys.DAILY_READ_PAGES_COUNT] ?: 0) else 0
            prefs[Keys.DAILY_READ_DATE] = today
            prefs[Keys.DAILY_READ_PAGES_COUNT] = (currentCount + delta).coerceAtLeast(0)
        }
    }

    suspend fun setDailyGoalPages(goal: Int) {
        context.dataStore.edit { it[Keys.DAILY_GOAL_PAGES] = goal.coerceIn(1, 50) }
    }

    suspend fun saveLastAudio(reciterId: String, surahNumber: Int) {
        context.dataStore.edit {
            it[Keys.LAST_RECITER_ID] = reciterId
            it[Keys.LAST_AUDIO_SURAH] = surahNumber
        }
    }

    suspend fun saveTasbeehState(
        count: Int,
        target: Int,
        total: Long,
        cycles: Int,
        zikrIndex: Int,
        vibrate: Boolean,
        clickSound: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.TASBEEH_COUNT] = count
            it[Keys.TASBEEH_TARGET] = target
            it[Keys.TASBEEH_TOTAL] = total
            it[Keys.TASBEEH_CYCLES] = cycles
            it[Keys.TASBEEH_ZIKR_INDEX] = zikrIndex
            it[Keys.TASBEEH_VIBRATE] = vibrate
            it[Keys.TASBEEH_SOUND] = clickSound
        }
    }

    suspend fun saveLocation(
        cityNameAr: String,
        cityEn: String,
        countryEn: String,
        lat: Double,
        lon: Double,
        useGps: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.CITY_NAME] = cityNameAr
            it[Keys.CITY_EN] = cityEn
            it[Keys.COUNTRY_EN] = countryEn
            it[Keys.LATITUDE] = lat
            it[Keys.LONGITUDE] = lon
            it[Keys.USE_GPS] = useGps
        }
    }

    suspend fun savePrayerSettings(
        methodId: Int,
        asrSchool: Int,
        preMinutes: Int,
        adhanSound: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.CALC_METHOD] = methodId
            it[Keys.ASR_SCHOOL] = asrSchool
            it[Keys.PRE_PRAYER_MIN] = preMinutes
            it[Keys.ADHAN_SOUND] = adhanSound
        }
    }

    suspend fun setHijriOffset(offset: Int) {
        context.dataStore.edit { it[Keys.HIJRI_OFFSET] = offset.coerceIn(-2, 2) }
    }

    suspend fun setPrayerNotify(prayerKey: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            when (prayerKey) {
                "fajr" -> prefs[Keys.NOTIFY_FAJR] = enabled
                "dhuhr" -> prefs[Keys.NOTIFY_DHUHR] = enabled
                "asr" -> prefs[Keys.NOTIFY_ASR] = enabled
                "maghrib" -> prefs[Keys.NOTIFY_MAGHRIB] = enabled
                "isha" -> prefs[Keys.NOTIFY_ISHA] = enabled
                "morning_azkar" -> prefs[Keys.NOTIFY_MORNING_AZKAR] = enabled
                "evening_azkar" -> prefs[Keys.NOTIFY_EVENING_AZKAR] = enabled
                "daily_reading" -> prefs[Keys.NOTIFY_DAILY_READING] = enabled
            }
        }
    }
}
