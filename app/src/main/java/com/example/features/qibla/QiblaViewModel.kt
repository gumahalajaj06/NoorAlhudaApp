package com.example.features.qibla

import android.content.Context
import android.hardware.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.PreferencesManager
import com.example.core.domain.QiblaCalculator
import com.example.core.domain.QiblaResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QiblaUiState(
    val cityName: String = "مكة المكرمة",
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val qibla: QiblaResult = QiblaCalculator.calculate(21.4225, 39.8262),
    val deviceAzimuthDegrees: Float = 0f,
    val hasCompassSensor: Boolean = true,
    val needsCalibration: Boolean = false,
    val accuracyLabel: String = "دقة جيدة"
) {
    /** Rotation angle for the Qibla needle relative to device heading */
    val needleRelativeAngle: Float
        get() = ((qibla.bearingDegrees.toFloat() - deviceAzimuthDegrees) + 360f) % 360f

    val isAlignedWithQibla: Boolean
        get() {
            val diff = needleRelativeAngle
            return diff <= 5f || diff >= 355f
        }
}

class QiblaViewModel(
    private val appContext: Context,
    private val prefsManager: PreferencesManager
) : ViewModel(), SensorEventListener {

    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var hasGravity = false
    private var hasMagnet = false
    private var smoothedAzimuth = 0f

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    init {
        val sensorAvailable = rotationSensor != null || (accelSensor != null && magnetSensor != null)
        _uiState.update { it.copy(hasCompassSensor = sensorAvailable) }

        viewModelScope.launch {
            prefsManager.preferencesFlow.collect { prefs ->
                val result = QiblaCalculator.calculate(prefs.latitude, prefs.longitude)
                _uiState.update {
                    it.copy(
                        cityName = prefs.selectedCityName,
                        latitude = prefs.latitude,
                        longitude = prefs.longitude,
                        qibla = result
                    )
                }
            }
        }
    }

    fun startListening() {
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else if (accelSensor != null && magnetSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            _uiState.update { it.copy(hasCompassSensor = false) }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                updateAzimuth(Math.toDegrees(orientationAngles[0].toDouble()).toFloat())
            }
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, 3)
                hasGravity = true
                computeFallbackOrientation()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                hasMagnet = true
                computeFallbackOrientation()
            }
        }
    }

    private fun computeFallbackOrientation() {
        if (!hasGravity || !hasMagnet) return
        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            updateAzimuth(Math.toDegrees(orientationAngles[0].toDouble()).toFloat())
        }
    }

    private fun updateAzimuth(rawDeg: Float) {
        val state = _uiState.value
        val decl = try {
            GeomagneticField(
                state.latitude.toFloat(),
                state.longitude.toFloat(),
                0f,
                System.currentTimeMillis()
            ).declination
        } catch (_: Exception) {
            0f
        }
        val trueHeading = ((rawDeg + decl) + 360f) % 360f
        // Smooth circular interpolation
        var delta = trueHeading - smoothedAzimuth
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        smoothedAzimuth = ((smoothedAzimuth + 0.18f * delta) + 360f) % 360f
        _uiState.update { it.copy(deviceAzimuthDegrees = smoothedAzimuth) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        val (needsCal, label) = when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> true to "تحتاج معايرة (حرّك الهاتف بشكل ∞)"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> true to "دقة منخفضة (حرّك الهاتف بشكل ∞)"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> false to "دقة متوسطة"
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> false to "دقة عالية"
            else -> false to "دقة جيدة"
        }
        _uiState.update { it.copy(needsCalibration = needsCal, accuracyLabel = label) }
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
