package com.example.core.di

import android.content.Context
import com.example.core.audio.AudioPlayerController
import com.example.core.database.AppDatabase
import com.example.core.database.PreferencesManager
import com.example.core.repositories.PrayerRepository
import com.example.core.repositories.QuranRepository

class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(appContext) }
    val quranRepository: QuranRepository by lazy { QuranRepository(appContext, database) }
    val prayerRepository: PrayerRepository by lazy { PrayerRepository(database) }
    val audioPlayerController: AudioPlayerController by lazy {
        AudioPlayerController(appContext, database, preferencesManager)
    }
}
