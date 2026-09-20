package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkBackground,
    secondary = DarkSecondary,
    onSecondary = DarkBackground,
    tertiary = MedicalBlue,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = CrimsonError,
    errorContainer = CrimsonErrorSubtle,
    onErrorContainer = CrimsonError
)

private val LightColorScheme = lightColorScheme(
    primary = NursingTealDark,
    onPrimary = PureWhite,
    secondary = MedicalBlue,
    onSecondary = PureWhite,
    tertiary = NursingTeal,
    background = AppleBackgroundLight,
    surface = AppleCardSurface,
    surfaceVariant = AppleCardElevated,
    onBackground = CharcoalPrimary,
    onSurface = CharcoalPrimary,
    onSurfaceVariant = SlateSecondary,
    outline = SlateBorder,
    outlineVariant = SlateBorderSubtle,
    error = CrimsonError,
    errorContainer = CrimsonErrorSubtle,
    onErrorContainer = CrimsonError
)

@Composable
fun CollegeErpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
