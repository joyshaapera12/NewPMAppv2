package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Core PitchMetrics Palette
val PitchGreenLight = Color(0xFF16A34A)
val PitchGreenStrongLight = Color(0xFF15803D)
val PitchGreenDark = Color(0xFF22C55E)
val PitchGreenStrongDark = Color(0xFF16A34A)

val PitchLiveRed = Color(0xFFEF4444)
val PitchLiveRedMuted = Color(0x33EF4444)

val PitchWarningAmber = Color(0xFFF59E0B)

val PitchAiVioletLight = Color(0xFF7C3AED)
val PitchAiVioletDark = Color(0xFF9B5CF5)
val PitchAiVioletAmoled = Color(0xFFA78BFA)

val PitchSecondaryBlueLight = Color(0xFF3B82F6)
val PitchSecondaryBlueDark = Color(0xFF60A5FA)

// Background & Surface - Light
val LightBg = Color(0xFFF7F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF1F3F6)
val LightBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F1623)
val LightTextMuted = Color(0xFF5B6472)

// Background & Surface - Dark (Default)
val DarkBg = Color(0xFF0F1623)
val DarkBgElevated = Color(0xFF141C2C)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceElevated = Color(0xFF1A2334)
val DarkBorder = Color(0xFF1D2A42)
val DarkTextPrimary = Color(0xFFF5F7FA)
val DarkTextMuted = Color(0xFF9AA5B5)

// Background & Surface - AMOLED
val AmoledBg = Color(0xFF000000)
val AmoledSurface = Color(0xFF050708)
val AmoledSurfaceElevated = Color(0xFF0D1117)
val AmoledBorder = Color(0xFF161B22)
val AmoledTextPrimary = Color(0xFFFFFFFF)
val AmoledTextMuted = Color(0xFF8B949E)

// Pitch Field Color
val PitchFieldDark = Color(0xFF0D2818)
val PitchFieldLines = Color(0x4422C55E)

@Immutable
data class PitchMetricsExtendedColors(
    val pitchGreen: Color,
    val pitchGreenStrong: Color,
    val liveRed: Color,
    val liveRedMuted: Color,
    val warningAmber: Color = PitchWarningAmber,
    val aiViolet: Color,
    val secondaryBlue: Color,
    val elevatedBackground: Color,
    val elevatedSurface: Color,
    val border: Color,
    val textMuted: Color,
    val glassBackground: Color,
    val glassBorder: Color,
    val pitchFieldBg: Color,
    val pitchFieldLines: Color
)

val LocalPitchMetricsColors = staticCompositionLocalOf {
    PitchMetricsExtendedColors(
        pitchGreen = PitchGreenDark,
        pitchGreenStrong = PitchGreenStrongDark,
        liveRed = PitchLiveRed,
        liveRedMuted = PitchLiveRedMuted,
        warningAmber = PitchWarningAmber,
        aiViolet = PitchAiVioletDark,
        secondaryBlue = PitchSecondaryBlueDark,
        elevatedBackground = DarkBgElevated,
        elevatedSurface = DarkSurfaceElevated,
        border = DarkBorder,
        textMuted = DarkTextMuted,
        glassBackground = Color(0xCC111827),
        glassBorder = Color(0x4D1D2A42),
        pitchFieldBg = PitchFieldDark,
        pitchFieldLines = PitchFieldLines
    )
}
