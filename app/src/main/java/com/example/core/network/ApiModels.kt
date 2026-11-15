package com.example.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- Al-Quran Cloud API Models ---
@JsonClass(generateAdapter = true)
data class QuranCloudMultiEditionResponse(
    val code: Int = 0,
    val status: String = "",
    val data: List<QuranCloudSurahData> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuranCloudSurahData(
    val number: Int = 0,
    val name: String = "",
    val englishName: String = "",
    val numberOfAyahs: Int = 0,
    val ayahs: List<QuranCloudAyah> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuranCloudAyah(
    val number: Int = 0, // global 1..6236
    val text: String = "",
    val numberInSurah: Int = 0,
    val juz: Int = 1,
    val page: Int = 1
)

@JsonClass(generateAdapter = true)
data class QuranCloudSearchResponse(
    val code: Int = 0,
    val status: String = "",
    val data: QuranCloudSearchData? = null
)

@JsonClass(generateAdapter = true)
data class QuranCloudSearchData(
    val count: Int = 0,
    val matches: List<QuranCloudSearchMatch> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuranCloudSearchMatch(
    val number: Int = 0,
    val text: String = "",
    val numberInSurah: Int = 0,
    val surah: QuranCloudSearchSurah? = null
)

@JsonClass(generateAdapter = true)
data class QuranCloudSearchSurah(
    val number: Int = 0,
    val name: String = ""
)

// --- The-Quran-Project/Quran-API Fallback ---
@JsonClass(generateAdapter = true)
data class QuranProjectSurahResponse(
    val surahName: String = "",
    val surahNameArabic: String = "",
    val surahNo: Int = 0,
    val totalAyah: Int = 0,
    val arabic1: List<String> = emptyList()
)

// --- Aladhan Prayer API Models ---
@JsonClass(generateAdapter = true)
data class AladhanTimingsResponse(
    val code: Int = 0,
    val status: String = "",
    val data: AladhanDayData? = null
)

@JsonClass(generateAdapter = true)
data class AladhanCalendarResponse(
    val code: Int = 0,
    val status: String = "",
    val data: List<AladhanDayData> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AladhanDayData(
    val timings: AladhanTimings? = null,
    val date: AladhanDate? = null
)

@JsonClass(generateAdapter = true)
data class AladhanTimings(
    @Json(name = "Fajr") val fajr: String = "",
    @Json(name = "Sunrise") val sunrise: String = "",
    @Json(name = "Dhuhr") val dhuhr: String = "",
    @Json(name = "Asr") val asr: String = "",
    @Json(name = "Maghrib") val maghrib: String = "",
    @Json(name = "Isha") val isha: String = ""
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    val gregorian: AladhanGregorian? = null,
    val hijri: AladhanHijri? = null
)

@JsonClass(generateAdapter = true)
data class AladhanGregorian(
    val date: String = "",
    val day: String = ""
)

@JsonClass(generateAdapter = true)
data class AladhanHijri(
    val date: String = "",
    val day: String = "",
    val year: String = "",
    val month: AladhanHijriMonth? = null,
    val weekday: AladhanHijriWeekday? = null
)

@JsonClass(generateAdapter = true)
data class AladhanHijriMonth(
    val number: Int = 1,
    val ar: String = "",
    val en: String = ""
)

@JsonClass(generateAdapter = true)
data class AladhanHijriWeekday(
    val ar: String = "",
    val en: String = ""
)

// --- MP3Quran v3 API Models ---
@JsonClass(generateAdapter = true)
data class Mp3QuranRecitersResponse(
    val reciters: List<Mp3QuranReciterDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Mp3QuranReciterDto(
    val id: Int = 0,
    val name: String = "",
    val moshaf: List<Mp3QuranMoshafDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Mp3QuranMoshafDto(
    val id: Int = 0,
    val name: String = "",
    val server: String = "",
    @Json(name = "surah_total") val surahTotal: Int = 0
)

// --- YousefAsalya Islamic-Pro-azkar-API ---
@JsonClass(generateAdapter = true)
data class RemoteAzkarCategoryDto(
    val id: Int = 0,
    val category: String = "",
    val array: List<RemoteAzkarItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RemoteAzkarItemDto(
    val id: Int = 0,
    val text: String = "",
    val count: Int = 1,
    val reference: String? = null
)
