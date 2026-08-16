package com.sarah.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary                 = SarahPrimary,
    onPrimary               = SarahOnPrimary,
    primaryContainer        = SarahPrimaryContainer,
    onPrimaryContainer      = SarahOnPrimaryContainer,
    inversePrimary          = SarahInversePrimary,
    secondary               = SarahSecondary,
    onSecondary             = SarahOnSecondary,
    secondaryContainer      = SarahSecondaryContainer,
    onSecondaryContainer    = SarahOnSecondaryContainer,
    tertiary                = SarahTertiary,
    onTertiary              = SarahOnTertiary,
    tertiaryContainer       = SarahTertiaryContainer,
    onTertiaryContainer     = SarahOnTertiaryContainer,
    background              = SarahBackground,
    onBackground            = SarahOnBackground,
    surface                 = SarahSurface,
    onSurface               = SarahOnSurface,
    surfaceVariant          = SarahSurfaceVariant,
    onSurfaceVariant        = SarahOnSurfaceVariant,
    surfaceTint             = SarahSurfaceTint,
    inverseSurface          = SarahInverseSurface,
    inverseOnSurface        = SarahInverseOnSurface,
    error                   = SarahError,
    onError                 = SarahOnError,
    errorContainer          = SarahErrorContainer,
    onErrorContainer        = SarahOnErrorContainer,
    outline                 = SarahOutline,
    outlineVariant          = SarahOutlineVariant,
    scrim                   = SarahInverseSurface,
    surfaceBright           = SarahSurfaceBright,
    surfaceContainerLowest  = SarahSurfaceContainerLowest,
    surfaceContainerLow     = SarahSurfaceContainerLow,
    surfaceContainer        = SarahSurfaceContainer,
    surfaceContainerHigh    = SarahSurfaceContainerHigh,
    surfaceContainerHighest = SarahSurfaceContainerHighest,
    surfaceDim              = SarahSurfaceDim,
)

@Composable
fun SarahTheme(
    // Sarah uses the light theme from the reference design.
    // Dark mode is not supported in the current release.
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Light status bar — dark icons on light background
            window.statusBarColor = SarahBackground.toArgb()
            window.navigationBarColor = SarahSurfaceContainerLowest.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
