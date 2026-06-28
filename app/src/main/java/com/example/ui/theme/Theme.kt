package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = NeonRose,
    background = RawCyberSlateDark,
    surface = RawCyberSlateMedium,
    onBackground = RawTextSilver,
    onSurface = Color.White,
    surfaceVariant = RawCyberSlateLight,
    onSurfaceVariant = RawTextGray
)

private val LightColorScheme = lightColorScheme(
    primary = BentoIndigoBg,
    secondary = NeonPurple,
    tertiary = NeonRose,
    background = Color(0xFFF1F5F9),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Disable dynamic colors to keep neon palette
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
