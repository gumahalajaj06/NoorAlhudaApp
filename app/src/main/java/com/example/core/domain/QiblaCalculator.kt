package com.example.core.domain

import kotlin.math.*

data class QiblaResult(
    val bearingDegrees: Double, // 0..360 clockwise from True North
    val distanceKm: Double,
    val cardinalDirectionArabic: String
)

object QiblaCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    fun calculate(latitude: Double, longitude: Double): QiblaResult {
        val lat1 = Math.toRadians(latitude)
        val lon1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lon2 = Math.toRadians(KAABA_LON)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0

        val dLat = lat2 - lat1
        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceKm = EARTH_RADIUS_KM * c

        return QiblaResult(
            bearingDegrees = bearing,
            distanceKm = distanceKm,
            cardinalDirectionArabic = toArabicDirection(bearing)
        )
    }

    private fun toArabicDirection(deg: Double): String {
        return when {
            deg < 22.5 || deg >= 337.5 -> "الشمال"
            deg < 67.5 -> "الشمال الشرقي"
            deg < 112.5 -> "الشرق"
            deg < 157.5 -> "الجنوب الشرقي"
            deg < 202.5 -> "الجنوب"
            deg < 247.5 -> "الجنوب الغربي"
            deg < 292.5 -> "الغرب"
            else -> "الشمال الغربي"
        }
    }
}
