package com.elendheim.melodymaker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Dark-first palette: near-black surfaces, warm amber primary, mint accents.
private val Amber = Color(0xFFFFB059)
private val AmberDim = Color(0xFF4A331A)
private val Mint = Color(0xFF7DE1C3)
private val MintDim = Color(0xFF1D3A32)
private val Ink = Color(0xFF0E0F13)
private val Surface1 = Color(0xFF16181F)
private val Surface2 = Color(0xFF1E212B)
private val TextMain = Color(0xFFECEDF1)
private val TextSoft = Color(0xFFA9ADBA)

private val DarkColors = darkColorScheme(
    primary = Amber,
    onPrimary = Color(0xFF241503),
    primaryContainer = AmberDim,
    onPrimaryContainer = Color(0xFFFFDDB5),
    secondary = Mint,
    onSecondary = Color(0xFF03241B),
    secondaryContainer = MintDim,
    onSecondaryContainer = Color(0xFFC2F5E3),
    tertiary = Color(0xFFB7A6FF),
    background = Ink,
    onBackground = TextMain,
    surface = Surface1,
    onSurface = TextMain,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextSoft,
    outline = Color(0xFF3A3E4B),
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
