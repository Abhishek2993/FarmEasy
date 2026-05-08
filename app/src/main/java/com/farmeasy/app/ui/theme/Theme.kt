package com.farmeasy.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = SurfaceLight,
    primaryContainer = CardGreenTint,
    onPrimaryContainer = ForestGreenDark,
    secondary = MossGreen,
    onSecondary = OnSurfaceLight,
    secondaryContainer = CardGreenTint,
    onSecondaryContainer = MossGreenDark,
    tertiary = WarmAmber,
    onTertiary = OnSurfaceLight,
    tertiaryContainer = CardAmberTint,
    onTertiaryContainer = WarmAmberDark,
    error = ErrorRed,
    onError = SurfaceLight,
    errorContainer = CardRedTint,
    onErrorContainer = ErrorRedDark,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OnSurfaceVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = MossGreen,
    onPrimary = ForestGreenDark,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = MossGreenLight,
    secondary = MossGreenLight,
    onSecondary = ForestGreenDark,
    secondaryContainer = MossGreenDark,
    onSecondaryContainer = MossGreenLight,
    tertiary = WarmAmberLight,
    onTertiary = WarmAmberDark,
    tertiaryContainer = WarmAmberDark,
    onTertiaryContainer = WarmAmberLight,
    error = ErrorRedLight,
    onError = ErrorRedDark,
    errorContainer = ErrorRedDark,
    onErrorContainer = ErrorRedLight,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OnSurfaceVariantDark
)

@Composable
fun FarmEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FarmEasyTypography,
        content = content
    )
}
