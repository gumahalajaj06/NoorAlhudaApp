package com.example.core.data

data class CityOption(
    val id: String,
    val nameArabic: String,
    val countryArabic: String,
    val countryEnglish: String,
    val cityEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val defaultMethodId: Int
)

object CitiesCatalog {
    val cities = listOf(
        CityOption("makkah", "مكة المكرمة", "السعودية", "Saudi Arabia", "Makkah", 21.4225, 39.8262, 4),
        CityOption("madinah", "المدينة المنورة", "السعودية", "Saudi Arabia", "Madinah", 24.4672, 39.6111, 4),
        CityOption("riyadh", "الرياض", "السعودية", "Saudi Arabia", "Riyadh", 24.7136, 46.6753, 4),
        CityOption("jeddah", "جدة", "السعودية", "Saudi Arabia", "Jeddah", 21.4858, 39.1925, 4),
        CityOption("cairo", "القاهرة", "مصر", "Egypt", "Cairo", 30.0444, 31.2357, 5),
        CityOption("alexandria", "الإسكندرية", "مصر", "Egypt", "Alexandria", 31.2001, 29.9187, 5),
        CityOption("jerusalem", "القدس", "فلسطين", "Palestine", "Jerusalem", 31.7683, 35.2137, 3),
        CityOption("amman", "عمّان", "الأردن", "Jordan", "Amman", 31.9454, 35.9284, 3),
        CityOption("damascus", "دمشق", "سوريا", "Syria", "Damascus", 33.5138, 36.2765, 3),
        CityOption("baghdad", "بغداد", "العراق", "Iraq", "Baghdad", 33.3152, 44.3661, 3),
        CityOption("dubai", "دبي", "الإمارات", "United Arab Emirates", "Dubai", 25.2048, 55.2708, 16),
        CityOption("abudhabi", "أبوظبي", "الإمارات", "United Arab Emirates", "Abu Dhabi", 24.4539, 54.3773, 16),
        CityOption("doha", "الدوحة", "قطر", "Qatar", "Doha", 25.2854, 51.5310, 10),
        CityOption("kuwait", "الكويت", "الكويت", "Kuwait", "Kuwait City", 29.3759, 47.9774, 9),
        CityOption("manama", "المنامة", "البحرين", "Bahrain", "Manama", 26.2285, 50.5860, 8),
        CityOption("muscat", "مسقط", "عُمان", "Oman", "Muscat", 23.5880, 58.3829, 8),
        CityOption("sanaa", "صنعاء", "اليمن", "Yemen", "Sanaa", 15.3694, 44.1910, 4),
        CityOption("beirut", "بيروت", "لبنان", "Lebanon", "Beirut", 33.8938, 35.5018, 3),
        CityOption("casablanca", "الدار البيضاء", "المغرب", "Morocco", "Casablanca", 33.5731, -7.5898, 21),
        CityOption("rabat", "الرباط", "المغرب", "Morocco", "Rabat", 34.0209, -6.8416, 21),
        CityOption("tunis", "تونس", "تونس", "Tunisia", "Tunis", 36.8065, 10.1815, 18),
        CityOption("algiers", "الجزائر", "الجزائر", "Algeria", "Algiers", 36.7538, 3.0588, 19),
        CityOption("tripoli", "طرابلس", "ليبيا", "Libya", "Tripoli", 32.8872, 13.1913, 5),
        CityOption("khartoum", "الخرطوم", "السودان", "Sudan", "Khartoum", 15.5007, 32.5599, 5),
        CityOption("istanbul", "إسطنبول", "تركيا", "Turkey", "Istanbul", 41.0082, 28.9784, 13),
        CityOption("london", "لندن", "المملكة المتحدة", "United Kingdom", "London", 51.5072, -0.1276, 3),
        CityOption("paris", "باريس", "فرنسا", "France", "Paris", 48.8566, 2.3522, 12),
        CityOption("newyork", "نيويورك", "الولايات المتحدة", "United States", "New York", 40.7128, -74.0060, 2),
        CityOption("jakarta", "جاكرتا", "إندونيسيا", "Indonesia", "Jakarta", -6.2088, 106.8456, 20),
        CityOption("kualalumpur", "كوالالمبور", "ماليزيا", "Malaysia", "Kuala Lumpur", 3.1390, 101.6869, 17)
    )

    fun findByNameOrDefault(name: String): CityOption {
        return cities.firstOrNull { it.nameArabic == name || it.cityEnglish.equals(name, true) } ?: cities.first()
    }
}
