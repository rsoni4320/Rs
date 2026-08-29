package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepBackground,
    primaryContainer = SurfaceCardElevated,
    onPrimaryContainer = PrimaryText,
    secondary = TechBlue,
    onSecondary = PrimaryText,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = PrimaryText,
    tertiary = PurpleAccent,
    onTertiary = PrimaryText,
    background = DeepBackground,
    onBackground = PrimaryText,
    surface = SecondaryBackground,
    onSurface = PrimaryText,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = SecondaryText,
    error = ErrorRed,
    onError = PrimaryText
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    JarvisTheme(content = content)
}

