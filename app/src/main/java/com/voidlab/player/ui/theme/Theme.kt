package com.voidlab.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VoidLabColorScheme = darkColorScheme(
    primary = VoidCyan,
    onPrimary = VoidBlack,
    primaryContainer = VoidCyanDark,
    onPrimaryContainer = VoidCyanLight,
    
    secondary = VoidPurple,
    onSecondary = VoidBlack,
    secondaryContainer = VoidPurpleDark,
    onSecondaryContainer = VoidPurpleLight,
    
    tertiary = VoidPink,
    onTertiary = VoidBlack,
    tertiaryContainer = VoidPinkDark,
    onTertiaryContainer = VoidPinkLight,
    
    error = VoidPink,
    onError = VoidBlack,
    
    background = VoidBlack,
    onBackground = Color(0xFFE0E0E0),
    
    surface = VoidDarkGray,
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = VoidGray,
    onSurfaceVariant = Color(0xFFB0B0B0),
    
    outline = VoidGray,
    outlineVariant = Color(0xFF404040)
)

@Composable
fun VoidLabTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VoidLabColorScheme,
        typography = Typography,
        content = content
    )
}
