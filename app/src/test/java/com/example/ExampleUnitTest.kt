package com.example

import com.example.core.data.AzkarCatalog
import com.example.core.data.DuasCatalog
import com.example.core.data.RecitersCatalog
import com.example.core.data.SurahCatalog
import com.example.core.domain.AstronomicalPrayerCalculator
import com.example.core.domain.HijriCalendarHelper
import com.example.core.domain.QiblaCalculator
import com.example.ui.theme.ArabicScriptFont
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun quran_catalog_has_exact_114_surahs_and_6236_ayahs() {
        assertEquals(114, SurahCatalog.surahs.size)
        assertEquals(6236, SurahCatalog.surahs.sumOf { it.ayahCount })
        assertEquals(1, SurahCatalog.getByNumber(1).startPage)
        assertEquals(604, SurahCatalog.getByNumber(114).startPage)
        assertEquals(262, SurahCatalog.getGlobalAyahNumber(2, 255)) // Ayat al-Kursi
    }

    @Test
    fun azkar_and_duas_cover_all_requested_categories() {
        assertEquals(12, AzkarCatalog.categories.size)
        AzkarCatalog.categories.forEach { cat ->
            assertTrue(
                "Category ${cat.id} should have items",
                AzkarCatalog.builtInAzkar.any { it.categoryId == cat.id }
            )
        }

        assertEquals(8, DuasCatalog.categories.size)
        DuasCatalog.categories.forEach { cat ->
            assertTrue(
                "Dua category ${cat.id} should have items",
                DuasCatalog.duas.any { it.categoryId == cat.id }
            )
        }
    }

    @Test
    fun astronomical_prayer_times_are_ordered_correctly() {
        // Makkah coordinates, UTC+3
        val schedule = AstronomicalPrayerCalculator.calculate(
            year = 2026,
            month = 11,
            day = 14,
            latitude = 21.4225,
            longitude = 39.8262,
            timeZoneOffsetHours = 3.0,
            methodId = 4,
            asrSchool = 0
        )
        val hanafi = AstronomicalPrayerCalculator.calculate(
            year = 2026,
            month = 11,
            day = 14,
            latitude = 21.4225,
            longitude = 39.8262,
            timeZoneOffsetHours = 3.0,
            methodId = 4,
            asrSchool = 1
        )
        assertTrue(schedule.fajr < schedule.sunrise)
        assertTrue(schedule.sunrise < schedule.dhuhr)
        assertTrue(schedule.dhuhr < schedule.asr)
        assertTrue(schedule.asr < schedule.maghrib)
        assertTrue(schedule.maghrib < schedule.isha)
        assertTrue(hanafi.asr >= schedule.asr)
    }

    @Test
    fun qibla_bearing_from_cairo_points_southeast() {
        // Cairo (30.0444, 31.2357) -> Makkah (21.4225, 39.8262) is ~136 degrees
        val qibla = QiblaCalculator.calculate(30.0444, 31.2357)
        assertTrue(qibla.bearingDegrees in 130.0..142.0)
        assertTrue(qibla.distanceKm in 1200.0..1350.0)
    }

    @Test
    fun hijri_calendar_converts_and_applies_offset() {
        val base = HijriCalendarHelper.fromGregorian(2026, 11, 14, 0)
        val plusOne = HijriCalendarHelper.fromGregorian(2026, 11, 14, 1)
        assertTrue(base.year >= 1447)
        assertTrue(base.monthNameArabic.isNotBlank())
        assertTrue(base.day != plusOne.day || base.month != plusOne.month)
    }

    @Test
    fun reciters_catalog_generates_valid_stream_urls() {
        val alafasy = RecitersCatalog.defaultReciters.first()
        assertEquals("https://server8.mp3quran.net/afs/001.mp3", alafasy.buildSurahStreamUrl(1))
        assertEquals("https://cdn.islamic.network/quran/audio/128/ar.alafasy/262.mp3", alafasy.buildAyahStreamUrl(262))
    }

    @Test
    fun curated_arabic_fonts_include_required_families_and_fallback() {
        val names = ArabicScriptFont.entries.map { it.nameEnglish }
        assertTrue(names.contains("Amiri"))
        assertTrue(names.contains("Noto Naskh Arabic"))
        assertTrue(names.contains("Scheherazade New"))
        assertEquals(ArabicScriptFont.SCHEHERAZADE_NEW, ArabicScriptFont.fromId("scheherazade_new"))
        assertEquals(ArabicScriptFont.NOTO_NASKH, ArabicScriptFont.fromId("noto_naskh"))
        assertEquals(ArabicScriptFont.AMIRI, ArabicScriptFont.fromId("unknown_font"))
    }
}
