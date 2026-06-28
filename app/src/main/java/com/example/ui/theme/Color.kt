package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Bento Grid Theme Raw Colors
val RawCyberSlateDark = Color(0xFF0F1115)       // Deep space bg
val RawCyberSlateMedium = Color(0xFF171921)     // Bento card background
val RawCyberSlateLight = Color(0xFF262936)      // Bento card border

val NeonCyan = Color(0xFF38BDF8)            // Sky cyan accent
val NeonPurple = Color(0xFF818CF8)          // Indigo-blue highlight
val NeonRose = Color(0xFFF43F5E)            // Rose indicator for High Risk

val NeonAmber = Color(0xFFF59E0B)           // Focus Score indicator
val NeonEmerald = Color(0xFF10B981)         // Status active green

val RawTextSilver = Color(0xFFE2E8F0)          // Slate 200
val RawTextGray = Color(0xFF94A3B8)            // Slate 400
val BentoIndigoBg = Color(0xFF4F46E5)       // Indigo primary accent

// Dynamic getters to support instant dark/light mode toggle everywhere seamlessly
val CyberSlateDark: Color @Composable get() = MaterialTheme.colorScheme.background
val CyberSlateMedium: Color @Composable get() = MaterialTheme.colorScheme.surface
val CyberSlateLight: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant

val TextSilver: Color @Composable get() = MaterialTheme.colorScheme.onBackground
val TextGray: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

val ThemeWhite: Color @Composable get() = MaterialTheme.colorScheme.onSurface
