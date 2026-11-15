package com.example.core.repositories

import com.example.core.database.AppDatabase
import com.example.core.database.CachedPrayerDayEntity
import com.example.core.domain.AstronomicalPrayerCalculator
import com.example.core.domain.DailyPrayerSchedule
import com.example.core.domain.HijriCalendarHelper
import com.example.core.domain.HijriDateModel
import com.example.core.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

data class PrayerSummaryState(
    val schedule: DailyPrayerSchedule,
    val hijriDate: HijriDateModel,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val remainingMillis: Long,
    val sourceLabel: String
)

data class MonthlyPrayerDay(
    val gregorianDay: Int,
    val hijriDay: String,
    val hijriMonth: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

class PrayerRepository(private val db: AppDatabase) {
    private val prayerDao = db.prayerCacheDao()

    suspend fun getTodayPrayerSummary(
        latitude: Double,
        longitude: Double,
        cityKey: String,
        methodId: Int,
        asrSchool: Int,
        hijriOffset: Int
    ): PrayerSummaryState = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dateStr = "%02d-%02d-%04d".format(day, month, year)
        val cacheKey = "${dateStr}_${cityKey}_${methodId}_${asrSchool}"

        val localHijri = HijriCalendarHelper.fromGregorian(year, month, day, hijriOffset)
        var schedule: DailyPrayerSchedule? = null
        var hijriModel = localHijri
        var sourceLabel = "حساب فلكي محلي"

        val cached = prayerDao.getCachedDay(cacheKey)
        if (cached != null) {
            schedule = DailyPrayerSchedule(
                dateDisplay = dateStr,
                fajr = cached.fajr,
                sunrise = cached.sunrise,
                dhuhr = cached.dhuhr,
                asr = cached.asr,
                maghrib = cached.maghrib,
                isha = cached.isha,
                isOfflineCalculated = false
            )
            sourceLabel = "Aladhan API (محفوظ محلياً)"
        } else {
            try {
                val res = NetworkModule.aladhanApi.getTimingsByCoordinates(
                    date = dateStr,
                    latitude = latitude,
                    longitude = longitude,
                    method = methodId,
                    school = asrSchool,
                    adjustment = hijriOffset
                )
                val data = res.data
                val timings = data?.timings
                if (timings != null) {
                    val clean = { raw: String -> raw.substringBefore(" ").trim() }
                    val parsed = DailyPrayerSchedule(
                        dateDisplay = dateStr,
                        fajr = clean(timings.fajr),
                        sunrise = clean(timings.sunrise),
                        dhuhr = clean(timings.dhuhr),
                        asr = clean(timings.asr),
                        maghrib = clean(timings.maghrib),
                        isha = clean(timings.isha),
                        isOfflineCalculated = false
                    )
                    schedule = parsed
                    sourceLabel = "Aladhan API"

                    val h = data.date?.hijri
                    if (h != null && h.day.isNotBlank()) {
                        val hDayInt = h.day.toIntOrNull() ?: localHijri.day
                        val hMonthInt = h.month?.number ?: localHijri.month
                        val hYearInt = h.year.toIntOrNull() ?: localHijri.year
                        val monthAr = h.month?.ar?.takeIf { it.isNotBlank() } ?: localHijri.monthNameArabic
                        val weekdayAr = h.weekday?.ar?.takeIf { it.isNotBlank() } ?: localHijri.weekdayArabic
                        val occasion = HijriCalendarHelper.occasions
                            .firstOrNull { it.hijriMonth == hMonthInt && it.hijriDay == hDayInt }?.titleArabic
                        hijriModel = localHijri.copy(
                            day = hDayInt,
                            month = hMonthInt,
                            year = hYearInt,
                            monthNameArabic = monthAr,
                            weekdayArabic = weekdayAr,
                            occasion = occasion
                        )
                    }

                    prayerDao.saveDay(
                        CachedPrayerDayEntity(
                            cacheKey = cacheKey,
                            dateKey = dateStr,
                            fajr = parsed.fajr,
                            sunrise = parsed.sunrise,
                            dhuhr = parsed.dhuhr,
                            asr = parsed.asr,
                            maghrib = parsed.maghrib,
                            isha = parsed.isha,
                            hijriDay = hijriModel.day.toString(),
                            hijriMonthAr = hijriModel.monthNameArabic,
                            hijriYear = hijriModel.year.toString(),
                            hijriWeekdayAr = hijriModel.weekdayArabic
                        )
                    )
                }
            } catch (_: Exception) {
                // Offline fallback via astronomical calculation
            }
        }

        val finalSchedule = schedule ?: AstronomicalPrayerCalculator.calculate(
            year = year,
            month = month,
            day = day,
            latitude = latitude,
            longitude = longitude,
            methodId = methodId,
            asrSchool = asrSchool
        )

        val (nextName, nextTime, remainingMs) = computeNextPrayer(finalSchedule)
        PrayerSummaryState(
            schedule = finalSchedule,
            hijriDate = hijriModel,
            nextPrayerName = nextName,
            nextPrayerTime = nextTime,
            remainingMillis = remainingMs,
            sourceLabel = sourceLabel
        )
    }

    suspend fun getMonthlySchedule(
        year: Int,
        month: Int,
        latitude: Double,
        longitude: Double,
        methodId: Int,
        asrSchool: Int,
        hijriOffset: Int
    ): List<MonthlyPrayerDay> = withContext(Dispatchers.IO) {
        try {
            val res = NetworkModule.aladhanApi.getMonthlyCalendar(
                year = year,
                month = month,
                latitude = latitude,
                longitude = longitude,
                method = methodId,
                school = asrSchool
            )
            if (res.data.isNotEmpty()) {
                val clean = { raw: String -> raw.substringBefore(" ").trim() }
                return@withContext res.data.mapIndexed { idx, dayData ->
                    val t = dayData.timings
                    val h = dayData.date?.hijri
                    val gDay = dayData.date?.gregorian?.day?.toIntOrNull() ?: (idx + 1)
                    MonthlyPrayerDay(
                        gregorianDay = gDay,
                        hijriDay = h?.day ?: "",
                        hijriMonth = h?.month?.ar ?: "",
                        fajr = clean(t?.fajr ?: "--:--"),
                        sunrise = clean(t?.sunrise ?: "--:--"),
                        dhuhr = clean(t?.dhuhr ?: "--:--"),
                        asr = clean(t?.asr ?: "--:--"),
                        maghrib = clean(t?.maghrib ?: "--:--"),
                        isha = clean(t?.isha ?: "--:--")
                    )
                }
            }
        } catch (_: Exception) {
            // Fall through to astronomical monthly computation
        }

        val cal = Calendar.getInstance().apply {
            set(year, month - 1, 1)
        }
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..daysInMonth).map { day ->
            val calc = AstronomicalPrayerCalculator.calculate(
                year = year,
                month = month,
                day = day,
                latitude = latitude,
                longitude = longitude,
                methodId = methodId,
                asrSchool = asrSchool
            )
            val h = HijriCalendarHelper.fromGregorian(year, month, day, hijriOffset)
            MonthlyPrayerDay(
                gregorianDay = day,
                hijriDay = h.day.toString(),
                hijriMonth = h.monthNameArabic,
                fajr = calc.fajr,
                sunrise = calc.sunrise,
                dhuhr = calc.dhuhr,
                asr = calc.asr,
                maghrib = calc.maghrib,
                isha = calc.isha
            )
        }
    }

    fun computeNextPrayer(schedule: DailyPrayerSchedule): Triple<String, String, Long> {
        val now = Calendar.getInstance()
        val entries = listOf(
            "الفجر" to schedule.fajr,
            "الشروق" to schedule.sunrise,
            "الظهر" to schedule.dhuhr,
            "العصر" to schedule.asr,
            "المغرب" to schedule.maghrib,
            "العشاء" to schedule.isha
        )

        for ((name, timeStr) in entries) {
            val parts = timeStr.split(":")
            if (parts.size == 2) {
                val h = parts[0].toIntOrNull() ?: continue
                val m = parts[1].toIntOrNull() ?: continue
                val target = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, h)
                    set(Calendar.MINUTE, m)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (target.timeInMillis > now.timeInMillis) {
                    return Triple(name, timeStr, target.timeInMillis - now.timeInMillis)
                }
            }
        }

        // Next prayer is tomorrow's Fajr
        val fajrParts = schedule.fajr.split(":")
        val fh = fajrParts.getOrNull(0)?.toIntOrNull() ?: 5
        val fm = fajrParts.getOrNull(1)?.toIntOrNull() ?: 0
        val tomorrowFajr = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, fh)
            set(Calendar.MINUTE, fm)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Triple("الفجر", schedule.fajr, (tomorrowFajr.timeInMillis - now.timeInMillis).coerceAtLeast(0L))
    }
}
