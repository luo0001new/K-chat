package com.example.kchat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val iOSLightColorScheme = lightColorScheme(
    primary = iOSBlue,
    onPrimary = iOSLightText,
    primaryContainer = iOSBlue,
    onPrimaryContainer = iOSLightText,
    secondary = iOSGray,
    onSecondary = iOSDarkText,
    secondaryContainer = iOSGray,
    onSecondaryContainer = iOSDarkText,
    tertiary = iOSGreen,
    onTertiary = iOSLightText,
    background = iOSBackground,
    onBackground = iOSDarkText,
    surface = iOSCardBackground,
    onSurface = iOSDarkText,
    surfaceVariant = iOSGray,
    onSurfaceVariant = iOSDarkText,
    outline = iOSSeparator,
    outlineVariant = iOSThinSeparator,
    error = iOSRed,
    onError = iOSLightText
)

@Composable
fun KChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = iOSLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        @Suppress("DEPRECATION")
        window.statusBarColor = iOSInputBackground.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
