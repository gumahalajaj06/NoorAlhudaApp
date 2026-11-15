package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedAyahEntity::class,
        BookmarkEntity::class,
        FavoriteAyahEntity::class,
        FavoriteZikrEntity::class,
        TasbeehLogEntity::class,
        DownloadedAudioEntity::class,
        CachedPrayerDayEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun bookmarksDao(): BookmarksDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun tasbeehDao(): TasbeehDao
    abstract fun downloadsDao(): DownloadsDao
    abstract fun prayerCacheDao(): PrayerCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "noor_alhuda_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
