package com.example.navigation

import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object QuranIndexRoute
@Serializable data class SurahReaderRoute(val surahNumber: Int, val targetAyah: Int = 1)
@Serializable data class MushafRoute(val initialPage: Int = 0) // 0 means resume lastPage
@Serializable object AzkarRoute
@Serializable object TasbeehRoute
@Serializable object AudioRoute
@Serializable object MoreRoute
@Serializable object PrayerTimesRoute
@Serializable object QiblaRoute
@Serializable object HijriCalendarRoute
@Serializable object DuasRoute
@Serializable object FavoritesRoute
@Serializable object SettingsRoute
@Serializable object DownloadsRoute
@Serializable object PrivacyPolicyRoute
@Serializable object AboutRoute
