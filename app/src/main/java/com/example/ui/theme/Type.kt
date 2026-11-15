package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Bold)
)

val NotoNaskhArabicFontFamily = FontFamily(
    Font(R.font.noto_naskh_arabic, FontWeight.Normal),
    Font(R.font.noto_naskh_arabic, FontWeight.Medium),
    Font(R.font.noto_naskh_arabic, FontWeight.Bold)
)

val ScheherazadeNewFontFamily = FontFamily(
    Font(R.font.scheherazade_new, FontWeight.Normal),
    Font(R.font.scheherazade_new, FontWeight.Bold)
)

val LateefFontFamily = FontFamily(
    Font(R.font.lateef, FontWeight.Normal),
    Font(R.font.lateef, FontWeight.Bold)
)

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold)
)

enum class ArabicScriptFont(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val descriptionArabic: String,
    val fontFamily: FontFamily
) {
    AMIRI(
        id = "amiri",
        nameArabic = "الخط الأميري",
        nameEnglish = "Amiri",
        descriptionArabic = "خط نسخ تراثي أصيل مناسب للمصحف والأذكار",
        fontFamily = AmiriFontFamily
    ),
    NOTO_NASKH(
        id = "noto_naskh",
        nameArabic = "نوتو نسخ عربي",
        nameEnglish = "Noto Naskh Arabic",
        descriptionArabic = "خط نسخ عصري واضح وعالي التباين والمقروئية",
        fontFamily = NotoNaskhArabicFontFamily
    ),
    SCHEHERAZADE_NEW(
        id = "scheherazade_new",
        nameArabic = "شهرزاد الجديد",
        nameEnglish = "Scheherazade New",
        descriptionArabic = "خط مصاحف تقليدي بوضوح التشكيل والحركات",
        fontFamily = ScheherazadeNewFontFamily
    ),
    LATEEF(
        id = "lateef",
        nameArabic = "خط لطيف",
        nameEnglish = "Lateef",
        descriptionArabic = "خط نسخ ناعم ومريح للعين في القراءة الطويلة",
        fontFamily = LateefFontFamily
    ),
    CAIRO(
        id = "cairo",
        nameArabic = "خط القاهرة",
        nameEnglish = "Cairo",
        descriptionArabic = "خط هندسي حديث بسيط ومرتب",
        fontFamily = CairoFontFamily
    );

    companion object {
        fun fromId(id: String): ArabicScriptFont =
            entries.find { it.id == id } ?: AMIRI
    }
}

val NoorTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 46.sp
    ),
    displayMedium = TextStyle(
        fontFamily = AmiriFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 38.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoNaskhArabicFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
