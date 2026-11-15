package com.example.core.data

data class ReciterInfo(
    val id: String,
    val nameArabic: String,
    val styleArabic: String,
    val mp3QuranServerUrl: String, // e.g. https://server8.mp3quran.net/afs/
    val islamicNetworkEdition: String?, // e.g. ar.alafasy for ayah-by-ayah & surah CDN
    val supportsAyahByAyah: Boolean
) {
    fun buildSurahStreamUrl(surahNumber: Int): String {
        val padded = surahNumber.toString().padStart(3, '0')
        return if (mp3QuranServerUrl.endsWith("/")) {
            "${mp3QuranServerUrl}${padded}.mp3"
        } else {
            "${mp3QuranServerUrl}/${padded}.mp3"
        }
    }

    fun buildFallbackSurahUrl(surahNumber: Int): String? {
        return islamicNetworkEdition?.let {
            "https://cdn.islamic.network/quran/audio-surah/128/$it/$surahNumber.mp3"
        }
    }

    fun buildAyahStreamUrl(globalAyahNumber: Int): String? {
        return islamicNetworkEdition?.let {
            "https://cdn.islamic.network/quran/audio/128/$it/$globalAyahNumber.mp3"
        }
    }
}

object RecitersCatalog {
    val defaultReciters = listOf(
        ReciterInfo(
            id = "alafasy",
            nameArabic = "مشاري بن راشد العفاسي",
            styleArabic = "حفص عن عاصم - مرتل",
            mp3QuranServerUrl = "https://server8.mp3quran.net/afs/",
            islamicNetworkEdition = "ar.alafasy",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "husary",
            nameArabic = "محمود خليل الحصري",
            styleArabic = "حفص عن عاصم - مرتل",
            mp3QuranServerUrl = "https://server13.mp3quran.net/husr/",
            islamicNetworkEdition = "ar.husary",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "minshawi",
            nameArabic = "محمد صديق المنشاوي",
            styleArabic = "حفص عن عاصم - مرتل",
            mp3QuranServerUrl = "https://server10.mp3quran.net/minsh/",
            islamicNetworkEdition = "ar.minshawi",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "abdulbasit",
            nameArabic = "عبد الباسط عبد الصمد",
            styleArabic = "حفص عن عاصم - مرتل",
            mp3QuranServerUrl = "https://server7.mp3quran.net/basit/",
            islamicNetworkEdition = "ar.abdulbasitmurattal",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "sudais",
            nameArabic = "عبد الرحمن السديس",
            styleArabic = "حفص عن عاصم - الحرم المكي",
            mp3QuranServerUrl = "https://server11.mp3quran.net/sds/",
            islamicNetworkEdition = "ar.abdurrahmaansudais",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "shuraim",
            nameArabic = "سعود الشريم",
            styleArabic = "حفص عن عاصم - الحرم المكي",
            mp3QuranServerUrl = "https://server7.mp3quran.net/shur/",
            islamicNetworkEdition = "ar.saoodshuraym",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "maher",
            nameArabic = "ماهر المعيقلي",
            styleArabic = "حفص عن عاصم - مرتل",
            mp3QuranServerUrl = "https://server12.mp3quran.net/maher/",
            islamicNetworkEdition = "ar.mahermuaiqly",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "hudhaify",
            nameArabic = "علي بن عبد الرحمن الحذيفي",
            styleArabic = "حفص عن عاصم - المسجد النبوي",
            mp3QuranServerUrl = "https://server9.mp3quran.net/hthfi/",
            islamicNetworkEdition = "ar.hudhaify",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "ajamy",
            nameArabic = "أحمد بن علي العجمي",
            styleArabic = "حفص عن عاصم - مرتل",
            mp3QuranServerUrl = "https://server10.mp3quran.net/ajm/",
            islamicNetworkEdition = "ar.ahmedajamy",
            supportsAyahByAyah = true
        ),
        ReciterInfo(
            id = "dosari",
            nameArabic = "ياسر الدوسري",
            styleArabic = "حفص عن عاصم - الحرم المكي",
            mp3QuranServerUrl = "https://server11.mp3quran.net/yasser/",
            islamicNetworkEdition = null,
            supportsAyahByAyah = false
        ),
        ReciterInfo(
            id = "qatami",
            nameArabic = "ناصر القطامي",
            styleArabic = "حفص عن عاصم - مرتل",
            mp3QuranServerUrl = "https://server6.mp3quran.net/qtm/",
            islamicNetworkEdition = null,
            supportsAyahByAyah = false
        )
    )
}
