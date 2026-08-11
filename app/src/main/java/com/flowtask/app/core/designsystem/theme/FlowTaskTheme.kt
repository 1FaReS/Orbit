package com.flowtask.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = OrbitBlue,
    onPrimary = Color.White,
    primaryContainer = OrbitBerrySoft,
    onPrimaryContainer = Color(0xFF65123A),
    secondary = OrbitAqua,
    secondaryContainer = OrbitAquaSoft,
    onSecondaryContainer = Color(0xFF0B4A58),
    tertiary = OrbitLilac,
    tertiaryContainer = OrbitLilacSoft,
    onTertiaryContainer = Color(0xFF513170),
    background = OrbitBackground,
    onBackground = OrbitText,
    surface = OrbitSurface,
    onSurface = OrbitText,
    surfaceVariant = Color(0xFFFFF5F9),
    onSurfaceVariant = OrbitTextSecondary,
    outline = OrbitBorder,
    outlineVariant = Color(0xFFF6EDF3),
)

private val DarkColors = darkColorScheme(
    primary = OrbitBlueDark,
    onPrimary = Color(0xFF5E0933),
    primaryContainer = Color(0xFF7E1648),
    onPrimaryContainer = Color(0xFFFFD9E7),
    secondary = Color(0xFF92D7E1),
    secondaryContainer = Color(0xFF164D57),
    onSecondaryContainer = Color(0xFFB7EBF2),
    tertiary = Color(0xFFD3B2FD),
    tertiaryContainer = Color(0xFF513970),
    onTertiaryContainer = Color(0xFFF0E4FF),
    background = OrbitBackgroundDark,
    onBackground = OrbitTextDark,
    surface = OrbitSurfaceDark,
    onSurface = OrbitTextDark,
    surfaceVariant = OrbitSurfaceSecondaryDark,
    onSurfaceVariant = OrbitTextSecondaryDark,
    outline = OrbitBorderDark,
    outlineVariant = OrbitBorderDark,
)

@Composable
fun OrbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = OrbitTypography,
        shapes = OrbitShapes,
        content = content,
    )
}
