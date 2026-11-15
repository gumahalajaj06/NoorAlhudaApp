package com.example.features.home

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.SurahCatalog
import com.example.core.data.SurahInfo
import com.example.core.database.PreferencesManager
import com.example.core.database.UserPreferences
import com.example.core.domain.HijriCalendarHelper
import com.example.core.domain.HijriDateModel
import com.example.core.notifications.ReminderScheduler
import com.example.core.repositories.PrayerRepository
import com.example.core.repositories.PrayerSummaryState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class HomeUiState(
    val prefs: UserPreferences = UserPreferences(),
    val prayerSummary: PrayerSummaryState? = null,
    val hijriDate: HijriDateModel = HijriCalendarHelper.today(),
    val countdownText: String = "00:00:00",
    val locationStatusText: String = "مكة المكرمة (مدينة مختارة)",
    val isLoadingPrayer: Boolean = false,
    val featuredSurahs: List<SurahInfo> = SurahCatalog.surahs
)

class HomeViewModel(
    private val appContext: Context,
    private val prefsManager: PreferencesManager,
    private val prayerRepository: PrayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsManager.preferencesFlow.collect { prefs ->
                val locStatus = if (prefs.useGpsLocation) {
                    "${prefs.selectedCityName} (تحديد تلقائي GPS)"
                } else {
                    "${prefs.selectedCityName} (مدينة محفوظة)"
                }
                _uiState.update {
                    it.copy(
                        prefs = prefs,
                        locationStatusText = locStatus,
                        hijriDate = HijriCalendarHelper.today(prefs.hijriOffsetDays)
                    )
                }
                refreshPrayerSummary(prefs)
            }
        }

        // Live 1-second countdown ticker
        viewModelScope.launch {
            while (isActive) {
                val summary = _uiState.value.prayerSummary
                if (summary != null) {
                    val (_, _, remaining) = prayerRepository.computeNextPrayer(summary.schedule)
                    _uiState.update { it.copy(countdownText = formatDuration(remaining)) }
                }
                delay(1000L)
            }
        }
    }

    fun refreshPrayerSummary(prefs: UserPreferences = _uiState.value.prefs) {
        val cal = java.util.Calendar.getInstance()
        val instantSchedule = com.example.core.domain.AstronomicalPrayerCalculator.calculate(
            year = cal.get(java.util.Calendar.YEAR),
            month = cal.get(java.util.Calendar.MONTH) + 1,
            day = cal.get(java.util.Calendar.DAY_OF_MONTH),
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            methodId = prefs.calculationMethodId,
            asrSchool = prefs.asrSchool
        )
        val (nextName, nextTime, remaining) = prayerRepository.computeNextPrayer(instantSchedule)
        _uiState.update {
            it.copy(
                prayerSummary = PrayerSummaryState(
                    schedule = instantSchedule,
                    hijriDate = HijriCalendarHelper.today(prefs.hijriOffsetDays),
                    nextPrayerName = nextName,
                    nextPrayerTime = nextTime,
                    remainingMillis = remaining,
                    sourceLabel = "حساب فلكي محلي"
                ),
                countdownText = formatDuration(remaining),
                isLoadingPrayer = false
            )
        }

        viewModelScope.launch {
            val summary = prayerRepository.getTodayPrayerSummary(
                latitude = prefs.latitude,
                longitude = prefs.longitude,
                cityKey = prefs.selectedCityName,
                methodId = prefs.calculationMethodId,
                asrSchool = prefs.asrSchool,
                hijriOffset = prefs.hijriOffsetDays
            )
            _uiState.update {
                it.copy(
                    prayerSummary = summary,
                    hijriDate = summary.hijriDate,
                    countdownText = formatDuration(summary.remainingMillis),
                    isLoadingPrayer = false
                )
            }
            ReminderScheduler.syncAllReminders(appContext, prefs, summary.schedule)
        }
    }

    @SuppressLint("MissingPermission")
    fun detectGpsLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(appContext)
        fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModelScope.launch {
                        val cityLabel = resolveCityName(location.latitude, location.longitude)
                        prefsManager.saveLocation(
                            cityNameAr = cityLabel,
                            cityEn = cityLabel,
                            countryEn = "",
                            lat = location.latitude,
                            lon = location.longitude,
                            useGps = true
                        )
                    }
                }
            }
    }

    private suspend fun resolveCityName(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val results = Geocoder(appContext, Locale("ar")).getFromLocation(lat, lon, 1)
            val addr = results?.firstOrNull()
            addr?.locality ?: addr?.adminArea ?: addr?.countryName ?: "موقعك الحالي"
        } catch (_: Exception) {
            "موقعك الحالي"
        }
    }

    fun updateDailyGoal(goalPages: Int) {
        viewModelScope.launch {
            prefsManager.setDailyGoalPages(goalPages)
        }
    }

    fun markPageRead() {
        viewModelScope.launch {
            prefsManager.incrementDailyReadPages(1)
        }
    }

    private fun formatDuration(ms: Long): String {
        val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
        val hours = totalSeconds / 3600L
        val minutes = (totalSeconds % 3600L) / 60L
        val seconds = totalSeconds % 60L
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}
