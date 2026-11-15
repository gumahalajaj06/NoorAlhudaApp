package com.example.features.tasbeeh

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AppDatabase
import com.example.core.database.PreferencesManager
import com.example.core.database.TasbeehLogEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TasbeehUiState(
    val count: Int = 0,
    val target: Int = 33,
    val totalCount: Long = 0L,
    val cycles: Int = 0,
    val selectedZikrIndex: Int = 0,
    val vibrateEnabled: Boolean = true,
    val clickSoundEnabled: Boolean = false,
    val todayTotal: Int = 0,
    val recentLogs: List<TasbeehLogEntity> = emptyList()
) {
    val progressFraction: Float
        get() = if (target <= 0) 0f else (count.toFloat() / target.toFloat()).coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progressFraction * 100).toInt()
}

class TasbeehViewModel(
    private val appContext: Context,
    private val db: AppDatabase,
    private val prefsManager: PreferencesManager
) : ViewModel() {

    val azkarOptions = listOf(
        "سبحان الله",
        "الحمد لله",
        "الله أكبر",
        "لا إله إلا الله",
        "أستغفر الله",
        "اللهم صلِّ وسلم على نبينا محمد ﷺ"
    )

    private val _uiState = MutableStateFlow(TasbeehUiState())
    val uiState: StateFlow<TasbeehUiState> = _uiState.asStateFlow()

    private val toneGenerator: ToneGenerator? by lazy {
        try {
            ToneGenerator(AudioManager.STREAM_SYSTEM, 35)
        } catch (_: Exception) {
            null
        }
    }

    init {
        viewModelScope.launch {
            val initial = prefsManager.preferencesFlow.first()
            _uiState.update {
                it.copy(
                    count = initial.tasbeehCount,
                    target = initial.tasbeehTarget,
                    totalCount = initial.tasbeehTotal,
                    cycles = initial.tasbeehCycles,
                    selectedZikrIndex = initial.tasbeehZikrIndex.coerceIn(0, azkarOptions.lastIndex),
                    vibrateEnabled = initial.tasbeehVibrate,
                    clickSoundEnabled = initial.tasbeehClickSound
                )
            }
        }

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        viewModelScope.launch {
            db.tasbeehDao().observeTodayCount(todayKey).collect { today ->
                _uiState.update { it.copy(todayTotal = today) }
            }
        }
        viewModelScope.launch {
            db.tasbeehDao().observeRecentLogs().collect { logs ->
                _uiState.update { it.copy(recentLogs = logs) }
            }
        }
    }

    fun onIncrementTap() {
        val current = _uiState.value
        val nextRaw = current.count + 1
        val completedCycle = nextRaw >= current.target
        val nextCount = if (completedCycle) 0 else nextRaw
        val nextCycles = if (completedCycle) current.cycles + 1 else current.cycles
        val nextTotal = current.totalCount + 1

        _uiState.update {
            it.copy(
                count = nextCount,
                cycles = nextCycles,
                totalCount = nextTotal
            )
        }

        if (current.vibrateEnabled) performHaptic(completedCycle)
        if (current.clickSoundEnabled) performClickSound(completedCycle)

        persistState()
        logTap(azkarOptions[current.selectedZikrIndex], 1)
    }

    fun setTarget(target: Int) {
        val safe = target.coerceIn(1, 100000)
        _uiState.update {
            it.copy(
                target = safe,
                count = if (it.count >= safe) 0 else it.count
            )
        }
        persistState()
    }

    fun selectZikr(index: Int) {
        _uiState.update { it.copy(selectedZikrIndex = index.coerceIn(0, azkarOptions.lastIndex)) }
        persistState()
    }

    fun resetCounter() {
        _uiState.update { it.copy(count = 0, cycles = 0) }
        persistState()
    }

    fun toggleVibrate() {
        _uiState.update { it.copy(vibrateEnabled = !it.vibrateEnabled) }
        persistState()
    }

    fun toggleClickSound() {
        _uiState.update { it.copy(clickSoundEnabled = !it.clickSoundEnabled) }
        persistState()
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.tasbeehDao().clearAll()
            _uiState.update { it.copy(totalCount = 0L, cycles = 0, count = 0) }
            persistState()
        }
    }

    private fun persistState() {
        val s = _uiState.value
        viewModelScope.launch {
            prefsManager.saveTasbeehState(
                count = s.count,
                target = s.target,
                total = s.totalCount,
                cycles = s.cycles,
                zikrIndex = s.selectedZikrIndex,
                vibrate = s.vibrateEnabled,
                clickSound = s.clickSoundEnabled
            )
        }
    }

    private fun logTap(zikrText: String, delta: Int) {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        viewModelScope.launch {
            db.tasbeehDao().insertLog(
                TasbeehLogEntity(
                    zikrText = zikrText,
                    count = delta,
                    dateKey = todayKey
                )
            )
        }
    }

    private fun performHaptic(strong: Boolean) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (!vibrator.hasVibrator()) return
            val duration = if (strong) 65L else 22L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (_: Exception) {
        }
    }

    private fun performClickSound(completedCycle: Boolean) {
        try {
            toneGenerator?.startTone(
                if (completedCycle) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP,
                if (completedCycle) 90 else 30
            )
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        try {
            toneGenerator?.release()
        } catch (_: Exception) {
        }
        super.onCleared()
    }
}
