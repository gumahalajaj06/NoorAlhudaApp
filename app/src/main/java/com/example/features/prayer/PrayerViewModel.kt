package com.example.features.prayer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.CityOption
import com.example.core.database.PreferencesManager
import com.example.core.database.UserPreferences
import com.example.core.notifications.ReminderScheduler
import com.example.core.repositories.MonthlyPrayerDay
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
import java.util.Calendar
import java.util.Locale

data class PrayerUiState(
    val prefs: UserPreferences = UserPreferences(),
    val todaySummary: PrayerSummaryState? = null,
    val monthlyDays: List<MonthlyPrayerDay> = emptyList(),
    val countdownText: String = "00:00:00",
    val isLoading: Boolean = true,
    val isLoadingMonth: Boolean = false,
    val locationMessage: String? = null
)

class PrayerViewModel(
    private val appContext: Context,
    private val prefsManager: PreferencesManager,
    private val prayerRepository: PrayerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsManager.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(prefs = prefs) }
                loadPrayerData(prefs)
            }
        }

        viewModelScope.launch {
            while (isActive) {
                _uiState.value.todaySummary?.let { summary ->
                    val (_, _, rem) = prayerRepository.computeNextPrayer(summary.schedule)
                    _uiState.update { it.copy(countdownText = formatDuration(rem)) }
                }
                delay(1000L)
            }
        }
    }

    private fun loadPrayerData(prefs: UserPreferences) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
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
                    todaySummary = summary,
                    countdownText = formatDuration(summary.remainingMillis),
                    isLoading = false
                )
            }
            ReminderScheduler.syncAllReminders(appContext, prefs, summary.schedule)
        }
    }

    fun loadMonthlyCalendar() {
        val prefs = _uiState.value.prefs
        val cal = Calendar.getInstance()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMonth = true) }
            val days = prayerRepository.getMonthlySchedule(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                latitude = prefs.latitude,
                longitude = prefs.longitude,
                methodId = prefs.calculationMethodId,
                asrSchool = prefs.asrSchool,
                hijriOffset = prefs.hijriOffsetDays
            )
            _uiState.update { it.copy(monthlyDays = days, isLoadingMonth = false) }
        }
    }

    fun selectManualCity(city: CityOption) {
        viewModelScope.launch {
            prefsManager.saveLocation(
                cityNameAr = city.nameArabic,
                cityEn = city.cityEnglish,
                countryEn = city.countryEnglish,
                lat = city.latitude,
                lon = city.longitude,
                useGps = false
            )
            val current = _uiState.value.prefs
            prefsManager.savePrayerSettings(
                methodId = city.defaultMethodId,
                asrSchool = current.asrSchool,
                preMinutes = current.prePrayerAlertMinutes,
                adhanSound = current.adhanSoundEnabled
            )
            _uiState.update { it.copy(locationMessage = "تم اختيار مدينة ${city.nameArabic}") }
        }
    }

    @SuppressLint("MissingPermission")
    fun useCurrentGpsLocation() {
        _uiState.update { it.copy(locationMessage = "جارٍ تحديد الموقع...") }
        val fused = LocationServices.getFusedLocationProviderClient(appContext)
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    viewModelScope.launch {
                        val name = resolveCityName(loc.latitude, loc.longitude)
                        prefsManager.saveLocation(
                            cityNameAr = name,
                            cityEn = name,
                            countryEn = "",
                            lat = loc.latitude,
                            lon = loc.longitude,
                            useGps = true
                        )
                        _uiState.update { it.copy(locationMessage = "تم تحديث الموقع: $name") }
                    }
                } else {
                    _uiState.update { it.copy(locationMessage = "تعذر الحصول على إحداثيات GPS، يمكنك اختيار المدينة يدوياً.") }
                }
            }
            .addOnFailureListener {
                _uiState.update { it.copy(locationMessage = "تعذر تحديد الموقع الجغرافي حالياً.") }
            }
    }

    private suspend fun resolveCityName(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val list = Geocoder(appContext, Locale("ar")).getFromLocation(lat, lon, 1)
            val addr = list?.firstOrNull()
            addr?.locality ?: addr?.adminArea ?: addr?.countryName ?: "موقعك الحالي"
        } catch (_: Exception) {
            "موقعك الحالي"
        }
    }

    fun updateCalculationMethod(methodId: Int) {
        val cur = _uiState.value.prefs
        viewModelScope.launch {
            prefsManager.savePrayerSettings(methodId, cur.asrSchool, cur.prePrayerAlertMinutes, cur.adhanSoundEnabled)
        }
    }

    fun updateAsrSchool(school: Int) {
        val cur = _uiState.value.prefs
        viewModelScope.launch {
            prefsManager.savePrayerSettings(cur.calculationMethodId, school, cur.prePrayerAlertMinutes, cur.adhanSoundEnabled)
        }
    }

    fun updatePrePrayerMinutes(minutes: Int) {
        val cur = _uiState.value.prefs
        viewModelScope.launch {
            prefsManager.savePrayerSettings(cur.calculationMethodId, cur.asrSchool, minutes, cur.adhanSoundEnabled)
        }
    }

    fun toggleAdhanSound(enabled: Boolean) {
        val cur = _uiState.value.prefs
        viewModelScope.launch {
            prefsManager.savePrayerSettings(cur.calculationMethodId, cur.asrSchool, cur.prePrayerAlertMinutes, enabled)
        }
    }

    fun togglePrayerNotification(prayerKey: String, enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.setPrayerNotify(prayerKey, enabled)
        }
    }

    private fun formatDuration(ms: Long): String {
        val total = (ms / 1000L).coerceAtLeast(0L)
        val h = total / 3600L
        val m = (total % 3600L) / 60L
        val s = total % 60L
        return "%02d:%02d:%02d".format(h, m, s)
    }
}
