package com.flowtask.app.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = FlowIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE6FF),
    onPrimaryContainer = Color(0xFF171153),
    secondary = FlowMint,
    tertiary = FlowCoral,
    background = FlowBackground,
    surface = FlowSurface,
    surfaceVariant = Color(0xFFF0EDFA),
)

private val DarkColors = darkColorScheme(
    primary = FlowIndigoDark,
    onPrimary = Color(0xFF2C2772),
    primaryContainer = Color(0xFF433E8D),
    onPrimaryContainer = Color(0xFFE4E1FF),
    secondary = Color(0xFFB5CCBF),
    tertiary = Color(0xFFFFB3AE),
    background = FlowBackgroundDark,
    surface = FlowSurfaceDark,
    surfaceVariant = Color(0xFF47464F),
)

@Composable
fun FlowTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = FlowTaskTypography,
        shapes = FlowTaskShapes,
        content = content,
    )
}
