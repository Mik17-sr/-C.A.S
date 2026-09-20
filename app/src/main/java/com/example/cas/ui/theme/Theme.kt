package com.example.cas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealSoft,
    onPrimaryContainer = StatusPublishedText,
    secondary = Graphite,
    onSecondary = Color.White,
    tertiary = Copper,
    onTertiary = Color.White,
    background = Paper,
    onBackground = InkDark,
    surface = CardSurface,
    onSurface = InkDark,
    onSurfaceVariant = InkMuted,
    outline = Border
)

@Composable
fun CASTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}