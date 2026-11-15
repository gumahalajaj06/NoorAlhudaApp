package com.example.core.domain

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

data class CalculationMethodConfig(
    val id: Int,
    val nameArabic: String,
    val fajrAngle: Double,
    val ishaAngle: Double?,
    val ishaMinutesAfterMaghrib: Int? = null
)

object PrayerCalculationMethods {
    val methods = listOf(
        CalculationMethodConfig(4, "أم القرى (مكة المكرمة)", 18.5, null, 90),
        CalculationMethodConfig(3, "رابطة العالم الإسلامي", 18.0, 17.0),
        CalculationMethodConfig(5, "الهيئة المصرية العامة للمساحة", 19.5, 17.5),
        CalculationMethodConfig(2, "الجمعية الإسلامية لأمريكا الشمالية (ISNA)", 15.0, 15.0),
        CalculationMethodConfig(1, "جامعة العلوم الإسلامية بكراتشي", 18.0, 18.0),
        CalculationMethodConfig(16, "دبي / الإمارات", 18.2, 18.2),
        CalculationMethodConfig(9, "الكويت", 18.0, 17.5),
        CalculationMethodConfig(10, "قطر", 18.0, null, 90),
        CalculationMethodConfig(13, "رئاسة الشؤون الدينية التركية", 18.0, 17.0)
    )

    fun byId(id: Int): CalculationMethodConfig = methods.firstOrNull { it.id == id } ?: methods.first()
}

data class DailyPrayerSchedule(
    val dateDisplay: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val isOfflineCalculated: Boolean = false
)

/**
 * Astronomical solar-position prayer time calculator (used as instant offline engine
 * and fallback whenever Aladhan API is unreachable).
 */
object AstronomicalPrayerCalculator {

    fun calculate(
        year: Int,
        month: Int, // 1..12
        day: Int,
        latitude: Double,
        longitude: Double,
        timeZoneOffsetHours: Double = TimeZone.getDefault().getOffset(
            Calendar.getInstance().apply { set(year, month - 1, day) }.timeInMillis
        ) / 3600000.0,
        methodId: Int = 4,
        asrSchool: Int = 0 // 0 = Shafi (1x), 1 = Hanafi (2x)
    ): DailyPrayerSchedule {
        val method = PrayerCalculationMethods.byId(methodId)
        val julianDay = toJulianDay(year, month, day) - longitude / (15.0 * 24.0)
        val (declination, eqOfTime) = sunPosition(julianDay)

        val transit = 12.0 + timeZoneOffsetHours - longitude / 15.0 - eqOfTime
        val sunriseAngle = 0.833 // standard refraction + solar radius
        val sunriseHourAngle = hourAngle(latitude, declination, sunriseAngle)
        val sunrise = transit - sunriseHourAngle
        val sunset = transit + sunriseHourAngle

        val fajrHourAngle = hourAngle(latitude, declination, method.fajrAngle)
        val fajr = transit - fajrHourAngle

        val asrShadowFactor = if (asrSchool == 1) 2.0 else 1.0
        val asrAltAngle = Math.toDegrees(atan(1.0 / (asrShadowFactor + tan(Math.toRadians(abs(latitude - declination))))))
        val asrHourAngle = hourAngle(latitude, declination, -asrAltAngle)
        val asr = transit + asrHourAngle

        val maghrib = sunset + (1.0 / 60.0)
        val isha = if (method.ishaMinutesAfterMaghrib != null) {
            maghrib + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            val ishaAngle = method.ishaAngle ?: 17.5
            transit + hourAngle(latitude, declination, ishaAngle)
        }

        val dateStr = "%02d-%02d-%04d".format(day, month, year)
        return DailyPrayerSchedule(
            dateDisplay = dateStr,
            fajr = formatHour(fajr),
            sunrise = formatHour(sunrise),
            dhuhr = formatHour(transit + 2.0 / 60.0),
            asr = formatHour(asr),
            maghrib = formatHour(maghrib),
            isha = formatHour(isha),
            isOfflineCalculated = true
        )
    }

    private fun toJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2.0 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqTime = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        return decl to eqTime
    }

    private fun hourAngle(latitude: Double, decl: Double, angleBelowHorizon: Double): Double {
        val latRad = Math.toRadians(latitude)
        val declRad = Math.toRadians(decl)
        val targetRad = Math.toRadians(-angleBelowHorizon)
        val cosH = ((sin(targetRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad)))
            .coerceIn(-0.9999, 0.9999)
        return Math.toDegrees(acos(cosH)) / 15.0
    }

    private fun fixAngle(a: Double): Double = (a % 360.0 + 360.0) % 360.0
    private fun fixHour(a: Double): Double = (a % 24.0 + 24.0) % 24.0

    private fun formatHour(time: Double): String {
        val fixed = fixHour(time)
        var hours = floor(fixed).toInt()
        var minutes = ((fixed - hours) * 60.0).roundToInt()
        if (minutes == 60) {
            hours = (hours + 1) % 24
            minutes = 0
        }
        return "%02d:%02d".format(hours, minutes)
    }
}
