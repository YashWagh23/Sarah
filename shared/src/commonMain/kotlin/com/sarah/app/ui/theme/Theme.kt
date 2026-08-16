package com.sarah.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
