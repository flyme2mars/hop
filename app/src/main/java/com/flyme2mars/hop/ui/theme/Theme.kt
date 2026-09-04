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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val InkDarkScheme = darkColorScheme(
    primary = FallbackPrimaryDark,
    onPrimary = FallbackOnPrimaryDark,
    primaryContainer = InkDarkContainerHi,
    onPrimaryContainer = InkDarkOn,
    secondary = InkDarkMuted,
    onSecondary = InkDarkBg,
    secondaryContainer = InkDarkContainerHi,
    onSecondaryContainer = InkDarkOn,
    tertiary = InkDarkMuted,
    onTertiary = InkDarkBg,
    tertiaryContainer = InkDarkContainer,
    onTertiaryContainer = InkDarkOn,
    background = InkDarkBg,
    onBackground = InkDarkOn,
    surface = InkDarkSurface,
    onSurface = InkDarkOn,
    surfaceVariant = InkDarkContainer,
    onSurfaceVariant = InkDarkMuted,
    surfaceContainerLowest = InkDarkBg,
    surfaceContainerLow = InkDarkSurface,
    surfaceContainer = InkDarkContainer,
    surfaceContainerHigh = InkDarkContainerHi,
    surfaceContainerHighest = InkDarkContainerHi,
    outline = InkDarkHairline,
    outlineVariant = InkDarkHairline,
    inverseSurface = InkLightSurface,
    inverseOnSurface = InkLightOn,
    inversePrimary = FallbackPrimaryLight,
    scrim = Color.Black,
)

private val InkLightScheme = lightColorScheme(
    primary = FallbackPrimaryLight,
    onPrimary = FallbackOnPrimaryLight,
    primaryContainer = InkLightContainerHi,
    onPrimaryContainer = InkLightOn,
    secondary = InkLightMuted,
    onSecondary = InkLightSurface,
    secondaryContainer = InkLightContainerHi,
    onSecondaryContainer = InkLightOn,
    tertiary = InkLightMuted,
    onTertiary = InkLightSurface,
    tertiaryContainer = InkLightContainer,
    onTertiaryContainer = InkLightOn,
    background = InkLightBg,
    onBackground = InkLightOn,
    surface = InkLightSurface,
    onSurface = InkLightOn,
    surfaceVariant = InkLightContainer,
    onSurfaceVariant = InkLightMuted,
    surfaceContainerLowest = InkLightSurface,
    surfaceContainerLow = InkLightSurface,
    surfaceContainer = InkLightContainer,
    surfaceContainerHigh = InkLightContainerHi,
    surfaceContainerHighest = InkLightContainerHi,
    outline = InkLightHairline,
    outlineVariant = InkLightHairline,
    inverseSurface = InkDarkSurface,
    inverseOnSurface = InkDarkOn,
    inversePrimary = FallbackPrimaryDark,
    scrim = InkDarkBg,
)

/** Surfaces stay locked ink. Dynamic color may tint primary / secondary / tertiary only. */
private fun ColorScheme.withLockedInk(base: ColorScheme): ColorScheme = base.copy(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
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
    val ink = if (darkTheme) InkDarkScheme else InkLightScheme
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dynamic.withLockedInk(ink)
    } else {
        ink
    }

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
                color = colorScheme.primary.copy(alpha = 0.12f),
            ),
        ) {
            content()
        }
    }
}
