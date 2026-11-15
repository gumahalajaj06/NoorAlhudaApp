package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.core.di.AppContainer
import com.example.features.audio.AudioPlayerScreen
import com.example.features.audio.AudioViewModel
import com.example.features.azkar.AzkarScreen
import com.example.features.azkar.AzkarViewModel
import com.example.features.duas.DuasScreen
import com.example.features.duas.DuasViewModel
import com.example.features.hijri.HijriCalendarScreen
import com.example.features.home.HomeScreen
import com.example.features.home.HomeViewModel
import com.example.features.more.*
import com.example.features.mushaf.MushafScreen
import com.example.features.mushaf.MushafViewModel
import com.example.features.prayer.PrayerTimesScreen
import com.example.features.prayer.PrayerViewModel
import com.example.features.qibla.QiblaScreen
import com.example.features.qibla.QiblaViewModel
import com.example.features.quran.QuranScreen
import com.example.features.quran.QuranViewModel
import com.example.features.quran.SurahReaderScreen
import com.example.features.tasbeeh.TasbeehScreen
import com.example.features.tasbeeh.TasbeehViewModel
import com.example.navigation.*
import com.example.ui.components.BottomNavDestination
import com.example.ui.components.IslamicGradientBackground
import com.example.ui.components.NoorBottomNavigationBar
import com.example.ui.theme.NoorAlHudaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as NoorApplication).container
        setContent {
            NoorAlHudaRoot(container = container)
        }
    }
}

private class NoorViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container.appContext, container.preferencesManager, container.prayerRepository) as T
            modelClass.isAssignableFrom(QuranViewModel::class.java) ->
                QuranViewModel(container.quranRepository, container.preferencesManager) as T
            modelClass.isAssignableFrom(MushafViewModel::class.java) ->
                MushafViewModel(container.quranRepository, container.preferencesManager) as T
            modelClass.isAssignableFrom(AzkarViewModel::class.java) ->
                AzkarViewModel(container.database) as T
            modelClass.isAssignableFrom(TasbeehViewModel::class.java) ->
                TasbeehViewModel(container.appContext, container.database, container.preferencesManager) as T
            modelClass.isAssignableFrom(AudioViewModel::class.java) ->
                AudioViewModel(container.audioPlayerController, container.database) as T
            modelClass.isAssignableFrom(PrayerViewModel::class.java) ->
                PrayerViewModel(container.appContext, container.preferencesManager, container.prayerRepository) as T
            modelClass.isAssignableFrom(QiblaViewModel::class.java) ->
                QiblaViewModel(container.appContext, container.preferencesManager) as T
            modelClass.isAssignableFrom(DuasViewModel::class.java) ->
                DuasViewModel(container.database) as T
            modelClass.isAssignableFrom(MoreViewModel::class.java) ->
                MoreViewModel(
                    container.appContext,
                    container.database,
                    container.preferencesManager,
                    container.quranRepository,
                    container.audioPlayerController
                ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

@Composable
fun NoorAlHudaRoot(container: AppContainer) {
    val factory = remember(container) { NoorViewModelFactory(container) }
    val homeVm: HomeViewModel = viewModel(factory = factory)
    val quranVm: QuranViewModel = viewModel(factory = factory)
    val mushafVm: MushafViewModel = viewModel(factory = factory)
    val azkarVm: AzkarViewModel = viewModel(factory = factory)
    val tasbeehVm: TasbeehViewModel = viewModel(factory = factory)
    val audioVm: AudioViewModel = viewModel(factory = factory)
    val prayerVm: PrayerViewModel = viewModel(factory = factory)
    val qiblaVm: QiblaViewModel = viewModel(factory = factory)
    val duasVm: DuasViewModel = viewModel(factory = factory)
    val moreVm: MoreViewModel = viewModel(factory = factory)

    val moreState by moreVm.uiState.collectAsStateWithLifecycle()

    NoorAlHudaTheme(
        darkTheme = moreState.prefs.isDarkMode,
        scriptFontId = moreState.prefs.selectedArabicFont
    ) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        val bottomDestinations = remember {
            listOf(
                BottomNavDestination("quran", "القرآن", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_quran"),
                BottomNavDestination("azkar", "الأذكار", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_azkar"),
                BottomNavDestination("tasbeeh", "المسبحة", Icons.Filled.Adjust, Icons.Outlined.Adjust, "nav_tasbeeh"),
                BottomNavDestination("audio", "التلاوة", Icons.Filled.Headphones, Icons.Outlined.Headphones, "nav_audio"),
                BottomNavDestination("more", "المزيد", Icons.Filled.GridView, Icons.Outlined.GridView, "nav_more")
            )
        }

        val activeTabRoute = when {
            currentDestination?.hasRoute<QuranIndexRoute>() == true -> "quran"
            currentDestination?.hasRoute<AzkarRoute>() == true -> "azkar"
            currentDestination?.hasRoute<TasbeehRoute>() == true -> "tasbeeh"
            currentDestination?.hasRoute<AudioRoute>() == true -> "audio"
            currentDestination?.hasRoute<MoreRoute>() == true -> "more"
            else -> null
        }

        val showBottomBar = currentDestination?.let { dest ->
            dest.hasRoute<HomeRoute>() ||
                dest.hasRoute<QuranIndexRoute>() ||
                dest.hasRoute<AzkarRoute>() ||
                dest.hasRoute<TasbeehRoute>() ||
                dest.hasRoute<AudioRoute>() ||
                dest.hasRoute<MoreRoute>()
        } ?: true

        val navigateTopLevel: (Any) -> Unit = { routeObj ->
            navController.navigate(routeObj) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        IslamicGradientBackground {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showBottomBar) {
                        NoorBottomNavigationBar(
                            items = bottomDestinations,
                            currentRoute = activeTabRoute,
                            onSelect = { item ->
                                when (item.route) {
                                    "quran" -> navigateTopLevel(QuranIndexRoute)
                                    "azkar" -> navigateTopLevel(AzkarRoute)
                                    "tasbeeh" -> navigateTopLevel(TasbeehRoute)
                                    "audio" -> navigateTopLevel(AudioRoute)
                                    "more" -> navigateTopLevel(MoreRoute)
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .statusBarsPadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                tween(240)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Start,
                                tween(240)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                tween(240)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.End,
                                tween(240)
                            )
                        }
                    ) {
                        composable<HomeRoute> {
                            val homeState by homeVm.uiState.collectAsStateWithLifecycle()
                            HomeScreen(
                                state = homeState,
                                onDetectLocation = homeVm::detectGpsLocation,
                                onOpenMushaf = { page -> navController.navigate(MushafRoute(page)) },
                                onOpenLastSurah = { surah, ayah ->
                                    navController.navigate(SurahReaderRoute(surah, ayah))
                                },
                                onOpenQuranIndex = { navigateTopLevel(QuranIndexRoute) },
                                onOpenSurah = { surah -> navController.navigate(SurahReaderRoute(surah, 1)) },
                                onOpenAzkar = { navigateTopLevel(AzkarRoute) },
                                onOpenTasbeeh = { navigateTopLevel(TasbeehRoute) },
                                onOpenAudio = { navigateTopLevel(AudioRoute) },
                                onOpenQibla = { navController.navigate(QiblaRoute) },
                                onOpenPrayerDetails = { navController.navigate(PrayerTimesRoute) },
                                onOpenHijriCalendar = { navController.navigate(HijriCalendarRoute) },
                                onIncrementDailyRead = homeVm::markPageRead,
                                onUpdateDailyGoal = homeVm::updateDailyGoal
                            )
                        }

                        composable<QuranIndexRoute> {
                            val indexState by quranVm.indexState.collectAsStateWithLifecycle()
                            QuranScreen(
                                state = indexState,
                                onSearchChange = quranVm::onSearchQueryChange,
                                onFilterChange = quranVm::onFilterTypeChange,
                                onOpenSurah = { surah, ayah ->
                                    navController.navigate(SurahReaderRoute(surah, ayah))
                                },
                                onOpenMushaf = { navController.navigate(MushafRoute(0)) },
                                onGoHome = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(HomeRoute) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<SurahReaderRoute> { entry ->
                            val args = entry.toRoute<SurahReaderRoute>()
                            val readerState by quranVm.readerState.collectAsStateWithLifecycle()
                            SurahReaderScreen(
                                surahNumber = args.surahNumber,
                                targetAyah = args.targetAyah,
                                state = readerState,
                                onLoadSurah = quranVm::openSurah,
                                onBack = { navController.popBackStack() },
                                onBookmarkAyah = quranVm::saveLastReadAyah,
                                onToggleFavorite = quranVm::toggleFavoriteAyah,
                                onSelectTafsir = quranVm::selectAyahForTafsir,
                                onFontSizeChange = quranVm::updateFontSize,
                                onFontFamilyChange = quranVm::updateArabicFont,
                                onToggleNightMode = quranVm::toggleReaderNightMode,
                                onPlayAyahAudio = { s, a ->
                                    audioVm.playAyah(s, a)
                                },
                                onOpenInMushaf = { startPage ->
                                    navController.navigate(MushafRoute(startPage))
                                }
                            )
                        }

                        composable<MushafRoute> { entry ->
                            val args = entry.toRoute<MushafRoute>()
                            val mushafState by mushafVm.uiState.collectAsStateWithLifecycle()
                            MushafScreen(
                                initialPage = args.initialPage,
                                state = mushafState,
                                getPageModel = mushafVm::getPageModelForCoil,
                                getFallbackUrl = mushafVm::getFallbackPageUrl,
                                onPageChanged = mushafVm::onPageChanged,
                                onToggleBookmark = mushafVm::toggleBookmarkCurrentPage,
                                onDownloadCurrentPage = mushafVm::downloadCurrentPage,
                                onDownloadSurahPages = mushafVm::downloadSurahPages,
                                onClearStatus = mushafVm::clearStatusMessage,
                                onBack = {
                                    moreVm.refreshDownloadedMushafCount()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<AzkarRoute> {
                            val azkarState by azkarVm.uiState.collectAsStateWithLifecycle()
                            AzkarScreen(
                                state = azkarState,
                                selectedFontId = moreState.prefs.selectedArabicFont,
                                onSelectFont = moreVm::setArabicFont,
                                onSelectCategory = azkarVm::selectCategory,
                                onDecrementZikr = azkarVm::decrementZikr,
                                onResetZikr = azkarVm::resetZikrCount,
                                onResetCategory = azkarVm::resetCategoryCounts,
                                onToggleFavorite = azkarVm::toggleFavorite,
                                onSyncRemote = azkarVm::syncFromRemoteApi,
                                onClearSyncMessage = azkarVm::clearSyncMessage,
                                onGoHome = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(HomeRoute) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<TasbeehRoute> {
                            val tasbeehState by tasbeehVm.uiState.collectAsStateWithLifecycle()
                            TasbeehScreen(
                                state = tasbeehState,
                                azkarOptions = tasbeehVm.azkarOptions,
                                onTap = tasbeehVm::onIncrementTap,
                                onSelectTarget = tasbeehVm::setTarget,
                                onSelectZikr = tasbeehVm::selectZikr,
                                onReset = tasbeehVm::resetCounter,
                                onToggleVibrate = tasbeehVm::toggleVibrate,
                                onToggleSound = tasbeehVm::toggleClickSound,
                                onClearHistory = tasbeehVm::clearHistory,
                                onGoHome = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(HomeRoute) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<AudioRoute> {
                            val audioUi by audioVm.uiState.collectAsStateWithLifecycle()
                            val playback by audioVm.playbackState.collectAsStateWithLifecycle()
                            AudioPlayerScreen(
                                uiState = audioUi,
                                playback = playback,
                                onSelectReciter = audioVm::selectReciter,
                                onSearchSurah = audioVm::onSurahSearch,
                                onPlaySurah = audioVm::playSurah,
                                onPlayAyah = audioVm::playAyah,
                                onTogglePlayPause = audioVm::togglePlayPause,
                                onSeekTo = audioVm::seekTo,
                                onSkipForward = audioVm::skipForward10s,
                                onSkipBackward = audioVm::skipBackward10s,
                                onVolumeChange = audioVm::setVolume,
                                onToggleRepeat = audioVm::toggleRepeatOne,
                                onToggleAutoNext = audioVm::toggleAutoNext,
                                onDownloadSurah = audioVm::downloadSurah,
                                onClearError = audioVm::clearError,
                                onGoHome = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(HomeRoute) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<MoreRoute> {
                            LaunchedEffect(Unit) { moreVm.refreshDownloadedMushafCount() }
                            MoreScreen(
                                state = moreState,
                                onOpenQibla = { navController.navigate(QiblaRoute) },
                                onOpenPrayerTimes = { navController.navigate(PrayerTimesRoute) },
                                onOpenHijri = { navController.navigate(HijriCalendarRoute) },
                                onOpenDuas = { navController.navigate(DuasRoute) },
                                onOpenFavorites = { navController.navigate(FavoritesRoute) },
                                onOpenSettings = { navController.navigate(SettingsRoute) },
                                onOpenDownloads = { navController.navigate(DownloadsRoute) },
                                onOpenPrivacyPolicy = { navController.navigate(PrivacyPolicyRoute) },
                                onOpenAbout = { navController.navigate(AboutRoute) },
                                onToggleTheme = moreVm::setDarkMode,
                                onGoHome = {
                                    navController.navigate(HomeRoute) {
                                        popUpTo(HomeRoute) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable<PrayerTimesRoute> {
                            val prayerState by prayerVm.uiState.collectAsStateWithLifecycle()
                            PrayerTimesScreen(
                                state = prayerState,
                                onSelectCity = prayerVm::selectManualCity,
                                onRequestGps = prayerVm::useCurrentGpsLocation,
                                onSelectMethod = prayerVm::updateCalculationMethod,
                                onSelectAsrSchool = prayerVm::updateAsrSchool,
                                onUpdatePreMinutes = prayerVm::updatePrePrayerMinutes,
                                onToggleAdhanSound = prayerVm::toggleAdhanSound,
                                onTogglePrayerNotify = prayerVm::togglePrayerNotification,
                                onLoadMonthly = prayerVm::loadMonthlyCalendar,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<QiblaRoute> {
                            val qiblaState by qiblaVm.uiState.collectAsStateWithLifecycle()
                            QiblaScreen(
                                state = qiblaState,
                                onStartSensor = qiblaVm::startListening,
                                onStopSensor = qiblaVm::stopListening,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<HijriCalendarRoute> {
                            HijriCalendarScreen(
                                hijriOffsetDays = moreState.prefs.hijriOffsetDays,
                                onOffsetChange = moreVm::setHijriOffset,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<DuasRoute> {
                            val duasState by duasVm.uiState.collectAsStateWithLifecycle()
                            DuasScreen(
                                state = duasState,
                                onSelectCategory = duasVm::selectCategory,
                                onToggleFavorite = duasVm::toggleFavorite,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<FavoritesRoute> {
                            FavoritesScreen(
                                state = moreState,
                                onOpenAyah = { s, a -> navController.navigate(SurahReaderRoute(s, a)) },
                                onOpenMushafPage = { p -> navController.navigate(MushafRoute(p)) },
                                onRemoveAyah = moreVm::removeFavoriteAyah,
                                onRemoveZikr = moreVm::removeFavoriteZikr,
                                onRemoveBookmark = moreVm::removeBookmark,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<SettingsRoute> {
                            val playback by audioVm.playbackState.collectAsStateWithLifecycle()
                            SettingsScreen(
                                state = moreState,
                                audioVolume = playback.volume,
                                onToggleDarkMode = moreVm::setDarkMode,
                                onFontSizeChange = moreVm::setFontSize,
                                onArabicFontChange = moreVm::setArabicFont,
                                onLanguageChange = moreVm::setAppLanguage,
                                onToggleNotification = moreVm::setNotificationToggle,
                                onVolumeChange = moreVm::setAudioVolume,
                                onOpenPrayerSettings = { navController.navigate(PrayerTimesRoute) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<DownloadsRoute> {
                            DownloadsScreen(
                                state = moreState,
                                onDeleteAudio = moreVm::deleteDownloadedAudio,
                                onClearMushafPages = moreVm::clearMushafPagesCache,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<PrivacyPolicyRoute> {
                            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                        }

                        composable<AboutRoute> {
                            AboutScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
