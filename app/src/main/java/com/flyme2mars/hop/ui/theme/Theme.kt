package com.flyme2mars.hop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MonoLightColorScheme = lightColorScheme(
    primary = MonoLightPrimary,
    onPrimary = MonoLightOnPrimary,
    primaryContainer = MonoLightPrimaryContainer,
    onPrimaryContainer = MonoLightOnPrimaryContainer,
    secondary = MonoLightSecondary,
    onSecondary = MonoLightOnSecondary,
    secondaryContainer = MonoLightSecondaryContainer,
    onSecondaryContainer = MonoLightOnSecondaryContainer,
    tertiary = MonoLightTertiary,
    onTertiary = MonoLightOnTertiary,
    tertiaryContainer = MonoLightTertiaryContainer,
    onTertiaryContainer = MonoLightOnTertiaryContainer,
    background = MonoLightBackground,
    onBackground = MonoLightOnBackground,
    surface = MonoLightSurface,
    onSurface = MonoLightOnSurface,
    surfaceVariant = MonoLightSurfaceVariant,
    onSurfaceVariant = MonoLightOnSurfaceVariant,
    surfaceContainerLowest = MonoLightSurfaceContainerLowest,
    surfaceContainerLow = MonoLightSurfaceContainerLow,
    surfaceContainer = MonoLightSurfaceContainer,
    surfaceContainerHigh = MonoLightSurfaceContainerHigh,
    surfaceContainerHighest = MonoLightSurfaceContainerHighest,
    outline = MonoLightOutline,
    outlineVariant = MonoLightOutlineVariant,
    inverseSurface = MonoLightInverseSurface,
    inverseOnSurface = MonoLightInverseOnSurface,
    inversePrimary = MonoLightInversePrimary,
)

private val MonoDarkColorScheme = darkColorScheme(
    primary = MonoDarkPrimary,
    onPrimary = MonoDarkOnPrimary,
    primaryContainer = MonoDarkPrimaryContainer,
    onPrimaryContainer = MonoDarkOnPrimaryContainer,
    secondary = MonoDarkSecondary,
    onSecondary = MonoDarkOnSecondary,
    secondaryContainer = MonoDarkSecondaryContainer,
    onSecondaryContainer = MonoDarkOnSecondaryContainer,
    tertiary = MonoDarkTertiary,
    onTertiary = MonoDarkOnTertiary,
    tertiaryContainer = MonoDarkTertiaryContainer,
    onTertiaryContainer = MonoDarkOnTertiaryContainer,
    background = MonoDarkBackground,
    onBackground = MonoDarkOnBackground,
    surface = MonoDarkSurface,
    onSurface = MonoDarkOnSurface,
    surfaceVariant = MonoDarkSurfaceVariant,
    onSurfaceVariant = MonoDarkOnSurfaceVariant,
    surfaceContainerLowest = MonoDarkSurfaceContainerLowest,
    surfaceContainerLow = MonoDarkSurfaceContainerLow,
    surfaceContainer = MonoDarkSurfaceContainer,
    surfaceContainerHigh = MonoDarkSurfaceContainerHigh,
    surfaceContainerHighest = MonoDarkSurfaceContainerHighest,
    outline = MonoDarkOutline,
    outlineVariant = MonoDarkOutlineVariant,
    inverseSurface = MonoDarkInverseSurface,
    inverseOnSurface = MonoDarkInverseOnSurface,
    inversePrimary = MonoDarkInversePrimary,
)

private fun ColorScheme.deepenEverydayDark(): ColorScheme = copy(
    background = lerp(background, EverydayDarkFloor, 0.72f),
    surface = lerp(surface, EverydayDarkFloor, 0.72f),
    surfaceContainerLowest = lerp(surfaceContainerLowest, EverydayDarkFloor, 0.78f),
    surfaceContainerLow = lerp(surfaceContainerLow, EverydayDarkContainerLow, 0.7f),
    surfaceContainer = lerp(surfaceContainer, EverydayDarkContainer, 0.65f),
    surfaceContainerHigh = lerp(surfaceContainerHigh, EverydayDarkContainerHigh, 0.6f),
    surfaceContainerHighest = lerp(surfaceContainerHighest, EverydayDarkContainerHighest, 0.55f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    cutMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val rawScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MonoDarkColorScheme
        else -> MonoLightColorScheme
    }
    val colorScheme = if (darkTheme) rawScheme.deepenEverydayDark() else rawScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            val lightIcons = if (cutMode) false else !darkTheme
            controller.isAppearanceLightStatusBars = lightIcons
            controller.isAppearanceLightNavigationBars = lightIcons
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = HopShapes,
    ) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(
                color = colorScheme.primary.copy(alpha = HopTokens.RippleAlpha),
            ),
        ) {
            content()
        }
    }
}
