package com.elendheim.melodymaker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun MelodyMakerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        shapes = AppShapes,
        content = content
    )
}
