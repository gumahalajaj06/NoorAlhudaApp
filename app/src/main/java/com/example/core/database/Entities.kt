package com.example.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cached_ayahs",
    primaryKeys = ["surahNumber", "ayahNumber"],
    indices = [Index(value = ["surahNumber"])]
)
data class CachedAyahEntity(
    val surahNumber: Int,
    val ayahNumber: Int,
    val globalNumber: Int,
    val textUthmani: String,
    val tafsirMuyassar: String,
    val juz: Int,
    val page: Int,
    val hasSajda: Boolean
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // e.g. "MUSHAF_12" or "AYAH_2_255"
    val type: String, // "MUSHAF" or "AYAH"
    val surahNumber: Int,
    val ayahNumber: Int,
    val pageNumber: Int,
    val title: String,
    val subtitle: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_ayahs")
data class FavoriteAyahEntity(
    @PrimaryKey val key: String, // "surah_ayah"
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahName: String,
    val textUthmani: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_azkar")
data class FavoriteZikrEntity(
    @PrimaryKey val id: String,
    val categoryTitle: String,
    val text: String,
    val reference: String,
    val grade: String,
    val isDua: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbeeh_logs")
data class TasbeehLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val zikrText: String,
    val count: Int,
    val dateKey: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_audio")
data class DownloadedAudioEntity(
    @PrimaryKey val id: String, // "reciterId_surahNumber"
    val reciterId: String,
    val reciterName: String,
    val surahNumber: Int,
    val surahName: String,
    val localPath: String,
    val fileSizeBytes: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_prayer_days")
data class CachedPrayerDayEntity(
    @PrimaryKey val cacheKey: String, // "yyyy-MM-dd_city_method_school"
    val dateKey: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDay: String,
    val hijriMonthAr: String,
    val hijriYear: String,
    val hijriWeekdayAr: String,
    val updatedAt: Long = System.currentTimeMillis()
)
