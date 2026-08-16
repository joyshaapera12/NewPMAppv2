package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED
}

private val DarkColorScheme = darkColorScheme(
    primary = PitchGreenDark,
    onPrimary = Color.Black,
    primaryContainer = PitchGreenStrongDark,
    onPrimaryContainer = Color.White,
    secondary = PitchSecondaryBlueDark,
    onSecondary = Color.Black,
    tertiary = PitchAiVioletDark,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    error = PitchLiveRed,
    onError = Color.White
)

private val AmoledColorScheme = darkColorScheme(
    primary = PitchGreenDark,
    onPrimary = Color.Black,
    primaryContainer = PitchGreenStrongDark,
    onPrimaryContainer = Color.White,
    secondary = PitchSecondaryBlueDark,
    onSecondary = Color.Black,
    tertiary = PitchAiVioletAmoled,
    onTertiary = Color.Black,
    background = AmoledBg,
    onBackground = AmoledTextPrimary,
    surface = AmoledSurface,
    onSurface = AmoledTextPrimary,
    surfaceVariant = AmoledSurfaceElevated,
    onSurfaceVariant = AmoledTextMuted,
    outline = AmoledBorder,
    error = PitchLiveRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PitchGreenLight,
    onPrimary = Color.White,
    primaryContainer = PitchGreenStrongLight,
    onPrimaryContainer = Color.White,
    secondary = PitchSecondaryBlueLight,
    onSecondary = Color.White,
    tertiary = PitchAiVioletLight,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    error = PitchLiveRed,
    onError = Color.White
)

@Composable
fun PitchMetricsTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
    }

    val colorScheme = when {
        themeMode == AppThemeMode.AMOLED -> AmoledColorScheme
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = when {
        themeMode == AppThemeMode.AMOLED -> PitchMetricsExtendedColors(
            pitchGreen = PitchGreenDark,
            pitchGreenStrong = PitchGreenStrongDark,
            liveRed = PitchLiveRed,
            liveRedMuted = PitchLiveRedMuted,
            warningAmber = PitchWarningAmber,
            aiViolet = PitchAiVioletAmoled,
            secondaryBlue = PitchSecondaryBlueDark,
            elevatedBackground = AmoledSurfaceElevated,
            elevatedSurface = AmoledSurfaceElevated,
            border = AmoledBorder,
            textMuted = AmoledTextMuted,
            glassBackground = Color(0xDD050708),
            glassBorder = Color(0x44161B22),
            pitchFieldBg = Color(0xFF041208),
            pitchFieldLines = Color(0x3322C55E)
        )
        isDark -> PitchMetricsExtendedColors(
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
        else -> PitchMetricsExtendedColors(
            pitchGreen = PitchGreenLight,
            pitchGreenStrong = PitchGreenStrongLight,
            liveRed = PitchLiveRed,
            liveRedMuted = PitchLiveRedMuted,
            warningAmber = PitchWarningAmber,
            aiViolet = PitchAiVioletLight,
            secondaryBlue = PitchSecondaryBlueLight,
            elevatedBackground = LightSurfaceElevated,
            elevatedSurface = LightSurfaceElevated,
            border = LightBorder,
            textMuted = LightTextMuted,
            glassBackground = Color(0xEEFFFFFF),
            glassBorder = Color(0x66E2E8F0),
            pitchFieldBg = Color(0xFF1E5E3A),
            pitchFieldLines = Color(0x55FFFFFF)
        )
    }

    CompositionLocalProvider(LocalPitchMetricsColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object PitchMetricsTheme {
    val colors: PitchMetricsExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPitchMetricsColors.current
}
