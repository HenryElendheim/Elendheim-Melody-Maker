package com.elendheim.melodymaker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

// Dark-first palette built from the app logo: lavender, purple, deep violet.
private val Lavender = Color(0xFFCEB7FF)
private val Purple = Color(0xFF9570E4)
private val PurpleMid = Color(0xFF7254B2)
private val Violet = Color(0xFF3D2570)
private val Ink = Color(0xFF121019)
private val Surface1 = Color(0xFF191624)
private val Surface2 = Color(0xFF231E33)
private val TextMain = Color(0xFFECE9F4)
private val TextSoft = Color(0xFFABA4C0)

private val DarkColors = darkColorScheme(
    primary = Lavender,
    onPrimary = Color(0xFF2A1656),
    primaryContainer = Violet,
    onPrimaryContainer = Color(0xFFE9DEFF),
    secondary = Purple,
    onSecondary = Color(0xFF1E0F42),
    secondaryContainer = Violet,
    onSecondaryContainer = Color(0xFFE9DEFF),
    tertiary = PurpleMid,
    background = Ink,
    onBackground = TextMain,
    surface = Surface1,
    onSurface = TextMain,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSoft,
    outline = Color(0xFF453D5E),
    error = Color(0xFFFF8A80)
)

// Same purple identity, pushed further apart: black background, white text,
// brighter secondary text and outlines.
private val HighContrastColors = DarkColors.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF14101E),
    surfaceVariant = Color(0xFF272138),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFD9D4E8),
    onPrimary = Color(0xFF1D0D45),
    outline = Color(0xFF8F87AD)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

private fun TextStyle.scaled(factor: Float) = copy(
    fontSize = if (fontSize.isSpecified) fontSize * factor else fontSize,
    lineHeight = if (lineHeight.isSpecified) lineHeight * factor else lineHeight
)

private fun Typography.scaled(factor: Float) = copy(
    displayLarge = displayLarge.scaled(factor),
    displayMedium = displayMedium.scaled(factor),
    displaySmall = displaySmall.scaled(factor),
    headlineLarge = headlineLarge.scaled(factor),
    headlineMedium = headlineMedium.scaled(factor),
    headlineSmall = headlineSmall.scaled(factor),
    titleLarge = titleLarge.scaled(factor),
    titleMedium = titleMedium.scaled(factor),
    titleSmall = titleSmall.scaled(factor),
    bodyLarge = bodyLarge.scaled(factor),
    bodyMedium = bodyMedium.scaled(factor),
    bodySmall = bodySmall.scaled(factor),
    labelLarge = labelLarge.scaled(factor),
    labelMedium = labelMedium.scaled(factor),
    labelSmall = labelSmall.scaled(factor)
)

private val BaseTypography = Typography()
private val LargeTypography = BaseTypography.scaled(1.2f)

@Composable
fun MelodyMakerTheme(
    largeText: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (highContrast) HighContrastColors else DarkColors,
        shapes = AppShapes,
        typography = if (largeText) LargeTypography else BaseTypography,
        content = content
    )
}
