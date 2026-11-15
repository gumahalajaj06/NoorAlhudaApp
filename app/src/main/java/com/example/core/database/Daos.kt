package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    fun observeSurahAyahs(surahNumber: Int): Flow<List<CachedAyahEntity>>

    @Query("SELECT * FROM cached_ayahs WHERE surahNumber = :surahNumber ORDER BY ayahNumber ASC")
    suspend fun getSurahAyahsOnce(surahNumber: Int): List<CachedAyahEntity>

    @Query("SELECT COUNT(*) FROM cached_ayahs WHERE surahNumber = :surahNumber")
    suspend fun getSurahCachedCount(surahNumber: Int): Int

    @Query("SELECT * FROM cached_ayahs WHERE textUthmani LIKE '%' || :query || '%' LIMIT 60")
    suspend fun searchCachedAyahs(query: String): List<CachedAyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<CachedAyahEntity>)
}

@Dao
interface BookmarksDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun observeExists(id: String): Flow<Boolean>
}

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite_ayahs ORDER BY createdAt DESC")
    fun observeFavoriteAyahs(): Flow<List<FavoriteAyahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteAyah(item: FavoriteAyahEntity)

    @Query("DELETE FROM favorite_ayahs WHERE `key` = :key")
    suspend fun deleteFavoriteAyah(key: String)

    @Query("SELECT * FROM favorite_azkar ORDER BY createdAt DESC")
    fun observeFavoriteAzkar(): Flow<List<FavoriteZikrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteZikr(item: FavoriteZikrEntity)

    @Query("DELETE FROM favorite_azkar WHERE id = :id")
    suspend fun deleteFavoriteZikr(id: String)
}

@Dao
interface TasbeehDao {
    @Query("SELECT * FROM tasbeeh_logs ORDER BY timestamp DESC LIMIT 100")
    fun observeRecentLogs(): Flow<List<TasbeehLogEntity>>

    @Query("SELECT COALESCE(SUM(count), 0) FROM tasbeeh_logs WHERE dateKey = :dateKey")
    fun observeTodayCount(dateKey: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TasbeehLogEntity)

    @Query("DELETE FROM tasbeeh_logs")
    suspend fun clearAll()
}

@Dao
interface DownloadsDao {
    @Query("SELECT * FROM downloaded_audio ORDER BY downloadedAt DESC")
    fun observeDownloads(): Flow<List<DownloadedAudioEntity>>

    @Query("SELECT * FROM downloaded_audio WHERE reciterId = :reciterId AND surahNumber = :surahNumber LIMIT 1")
    suspend fun getDownload(reciterId: String, surahNumber: Int): DownloadedAudioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadedAudioEntity)

    @Query("DELETE FROM downloaded_audio WHERE id = :id")
    suspend fun deleteDownload(id: String)
}

@Dao
interface PrayerCacheDao {
    @Query("SELECT * FROM cached_prayer_days WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCachedDay(cacheKey: String): CachedPrayerDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDay(entity: CachedPrayerDayEntity)
}
