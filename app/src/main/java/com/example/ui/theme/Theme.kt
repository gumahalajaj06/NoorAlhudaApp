package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

val LocalScriptFontFamily = staticCompositionLocalOf<FontFamily> { AmiriFontFamily }

@Immutable
data class NoorExtendedColors(
    val gold: Color,
    val goldBorder: Color,
    val emerald: Color,
    val emeraldBright: Color,
    val deepBackgroundTop: Color,
    val deepBackgroundBottom: Color,
    val glassSurface: Color,
    val cardElevated: Color,
    val mutedText: Color
)

val LocalNoorColors = staticCompositionLocalOf {
    NoorExtendedColors(
        gold = GoldAccent,
        goldBorder = GoldAccent.copy(alpha = 0.35f),
        emerald = EmeraldPrimary,
        emeraldBright = EmeraldLight,
        deepBackgroundTop = DeepNavy,
        deepBackgroundBottom = DeepNavyElevated,
        glassSurface = CardNavyBlue.copy(alpha = 0.85f),
        cardElevated = CardNavyElevated,
        mutedText = TextSecondaryOnDark
    )
}

private val NoorDarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = IvoryWhite,
    primaryContainer = EmeraldPrimary,
    onPrimaryContainer = IvoryWhite,
    secondary = GoldAccent,
    onSecondary = DeepNavy,
    secondaryContainer = CardNavyElevated,
    onSecondaryContainer = GoldSoft,
    tertiary = GoldSoft,
    onTertiary = DeepNavy,
    background = DeepNavy,
    onBackground = TextOnDark,
    surface = DeepNavyElevated,
    onSurface = TextOnDark,
    surfaceVariant = CardNavyBlue,
    onSurfaceVariant = TextSecondaryOnDark,
    outline = GoldAccent.copy(alpha = 0.35f),
    outlineVariant = CardNavyElevated,
    error = ErrorRedDark,
    onError = DeepNavy
)

private val NoorLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = IvoryWhite,
    primaryContainer = EmeraldLight.copy(alpha = 0.14f),
    onPrimaryContainer = EmeraldPrimary,
    secondary = GoldDeep,
    onSecondary = IvoryWhite,
    secondaryContainer = GoldAccent.copy(alpha = 0.20f),
    onSecondaryContainer = DeepNavy,
    tertiary = EmeraldLight,
    onTertiary = IvoryWhite,
    background = IvoryWhite,
    onBackground = TextOnLight,
    surface = IvorySurface,
    onSurface = TextOnLight,
    surfaceVariant = IvoryCard,
    onSurfaceVariant = TextSecondaryOnLight,
    outline = GoldDeep.copy(alpha = 0.35f),
    outlineVariant = IvoryBorder,
    error = ErrorRedLight,
    onError = IvoryWhite
)

private val NoorShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun NoorAlHudaTheme(
    darkTheme: Boolean = true,
    scriptFontId: String = ArabicScriptFont.AMIRI.id,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NoorDarkColorScheme else NoorLightColorScheme
    val scriptFontFamily = ArabicScriptFont.fromId(scriptFontId).fontFamily
    val extendedColors = if (darkTheme) {
        NoorExtendedColors(
            gold = GoldAccent,
            goldBorder = GoldAccent.copy(alpha = 0.35f),
            emerald = EmeraldPrimary,
            emeraldBright = EmeraldLight,
            deepBackgroundTop = DeepNavy,
            deepBackgroundBottom = Color(0xFF0D213D),
            glassSurface = CardNavyBlue.copy(alpha = 0.88f),
            cardElevated = CardNavyElevated,
            mutedText = TextSecondaryOnDark
        )
    } else {
        NoorExtendedColors(
            gold = GoldDeep,
            goldBorder = GoldDeep.copy(alpha = 0.32f),
            emerald = EmeraldPrimary,
            emeraldBright = EmeraldLight,
            deepBackgroundTop = IvoryWhite,
            deepBackgroundBottom = Color(0xFFEBE4D3),
            glassSurface = IvorySurface.copy(alpha = 0.94f),
            cardElevated = IvoryCard,
            mutedText = TextSecondaryOnLight
        )
    }

    CompositionLocalProvider(
        LocalNoorColors provides extendedColors,
        LocalScriptFontFamily provides scriptFontFamily,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NoorTypography,
            shapes = NoorShapes,
            content = content
        )
    }
}
