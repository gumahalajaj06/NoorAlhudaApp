package com.example.core.domain

import android.os.Build
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar
import kotlin.math.floor

data class HijriDateModel(
    val day: Int,
    val month: Int,
    val year: Int,
    val monthNameArabic: String,
    val weekdayArabic: String,
    val gregorianDisplay: String,
    val occasion: String?
) {
    val formattedArabic: String
        get() = "$day $monthNameArabic $year هـ"
}

data class IslamicOccasion(
    val hijriMonth: Int,
    val hijriDay: Int,
    val titleArabic: String,
    val descriptionArabic: String
)

object HijriCalendarHelper {
    val hijriMonths = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val gregorianMonthsArabic = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    val weekdaysArabic = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    val occasions = listOf(
        IslamicOccasion(1, 1, "رأس السنة الهجرية", "بداية العام الهجري الجديد"),
        IslamicOccasion(1, 10, "يوم عاشوراء", "صيام العاشر من محرم وتاسوعاء قبله"),
        IslamicOccasion(3, 12, "شهر ربيع الأول", "ذكرى مولد النبي محمد ﷺ"),
        IslamicOccasion(7, 27, "ليلة الإسراء والمعراج", "ذكرى معجزة الإسراء والمعراج"),
        IslamicOccasion(8, 15, "ليلة النصف من شعبان", "فضل ليلة النصف من شعبان"),
        IslamicOccasion(9, 1, "غرة شهر رمضان المبارك", "بداية شهر الصيام والقيام"),
        IslamicOccasion(9, 27, "العشر الأواخر وليلة القدر", "تحري ليلة القدر في الوتر من العشر الأواخر"),
        IslamicOccasion(10, 1, "عيد الفطر المبارك", "أول أيام عيد الفطر السعيد"),
        IslamicOccasion(12, 9, "يوم عرفة", "أعظم أيام العام وصيامه لغير الحاج"),
        IslamicOccasion(12, 10, "عيد الأضحى المبارك", "يوم النحر وعيد الأضحى المبارك")
    )

    fun fromGregorian(year: Int, month: Int, day: Int, offsetDays: Int = 0): HijriDateModel {
        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        val weekday = weekdaysArabic[(cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)]
        val gregDisplay = "$day ${gregorianMonthsArabic[(month - 1).coerceIn(0, 11)]} $year م"

        val (hDay, hMonth, hYear) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDate = LocalDate.of(year, month, day).plusDays(offsetDays.toLong())
            val hijrah = HijrahDate.from(localDate)
            Triple(
                hijrah.get(ChronoField.DAY_OF_MONTH),
                hijrah.get(ChronoField.MONTH_OF_YEAR),
                hijrah.get(ChronoField.YEAR_OF_ERA)
            )
        } else {
            val adjustedCal = Calendar.getInstance().apply {
                set(year, month - 1, day)
                add(Calendar.DAY_OF_MONTH, offsetDays)
            }
            tabularGregorianToHijri(
                adjustedCal.get(Calendar.YEAR),
                adjustedCal.get(Calendar.MONTH) + 1,
                adjustedCal.get(Calendar.DAY_OF_MONTH)
            )
        }

        val monthName = hijriMonths[(hMonth - 1).coerceIn(0, 11)]
        val occasion = occasions.firstOrNull { it.hijriMonth == hMonth && it.hijriDay == hDay }?.titleArabic

        return HijriDateModel(
            day = hDay,
            month = hMonth,
            year = hYear,
            monthNameArabic = monthName,
            weekdayArabic = weekday,
            gregorianDisplay = gregDisplay,
            occasion = occasion
        )
    }

    fun today(offsetDays: Int = 0): HijriDateModel {
        val cal = Calendar.getInstance()
        return fromGregorian(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH),
            offsetDays
        )
    }

    private fun tabularGregorianToHijri(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
        val jd = if ((year > 1582) || (year == 1582 && month > 10) || (year == 1582 && month == 10 && day > 14)) {
            ((1461 * (year + 4800 + (month - 14) / 12)) / 4 +
                (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12 -
                (3 * ((year + 4900 + (month - 14) / 12) / 100)) / 4 +
                day - 32075)
        } else {
            (367 * year - (7 * (year + 5001 + (month - 9) / 7)) / 4 +
                (275 * month) / 9 + day + 1729777)
        }
        var l = jd - 1948440 + 10632
        val n = (l - 1) / 10631
        l = l - 10631 * n + 354
        val j = ((10985 - l) / 5316) * ((50 * l) / 17719) + (l / 5670) * ((43 * l) / 15238)
        l = l - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hMonth = (24 * l) / 709
        val hDay = l - (709 * hMonth) / 24
        val hYear = 30 * n + j - 30
        return Triple(hDay, hMonth, hYear)
    }
}
