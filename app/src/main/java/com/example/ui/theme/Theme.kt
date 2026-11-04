package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun SideLauncherTheme(
    accentColor: Color = AccentBlue,
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = Color.Black,
        primaryContainer = accentColor.copy(alpha = 0.25f),
        onPrimaryContainer = TextPrimary,
        secondary = accentColor,
        onSecondary = Color.Black,
        background = BackgroundDark,
        onBackground = TextPrimary,
        surface = SurfaceDark,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceElevatedDark,
        onSurfaceVariant = TextSecondary,
        outline = SurfaceBorderDark,
        error = DangerRed,
        onError = Color.White
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        typography = Typography,
        content = content
    )
}
