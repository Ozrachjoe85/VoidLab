package com.voidlab.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
    
    background = VoidBlack,
    onBackground = VoidCyan,
    
    surface = VoidBlackLight,
    onSurface = VoidCyan,
    surfaceVariant = VoidBlackLight,
    onSurfaceVariant = VoidCyanDark,
    
    error = VoidPink,
    onError = VoidBlack,
    errorContainer = VoidPinkDark,
    onErrorContainer = VoidPinkLight,
    
    outline = VoidCyanDark,
    outlineVariant = VoidBlackLight
)

@Composable
fun VoidLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = VoidLabColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
