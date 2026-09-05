package com.flyme2mars.hop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

object HopTheme {
    val colors: HopColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHopColors.current
}

private val HopShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private fun hopDarkScheme() = darkColorScheme(
    primary = HopAccentDark,
    onPrimary = HopAccentTextDark,
    secondary = HopAccentDark,
    onSecondary = HopAccentTextDark,
    background = HopBgDark,
    onBackground = HopTextPrimaryDark,
    surface = HopSurfaceDark,
    onSurface = HopTextPrimaryDark,
    surfaceVariant = HopSurfaceRaisedDark,
    onSurfaceVariant = HopTextSecondaryDark,
    surfaceContainer = HopSurfaceDark,
    surfaceContainerHigh = HopSurfaceRaisedDark,
    surfaceContainerHighest = HopSurfaceRaisedDark,
    surfaceContainerLow = HopSurfaceDark,
    surfaceContainerLowest = HopBgDark,
    outline = HopBorderDark,
    outlineVariant = HopBorderDark,
    error = HopDanger,
    onError = HopAccentTextDark,
    scrim = HopScrimDark,
)

private fun hopLightScheme() = lightColorScheme(
    primary = HopAccentLight,
    onPrimary = HopAccentTextLight,
    secondary = HopAccentLight,
    onSecondary = HopAccentTextLight,
    background = HopBgLight,
    onBackground = HopTextPrimaryLight,
    surface = HopSurfaceLight,
    onSurface = HopTextPrimaryLight,
    surfaceVariant = HopSurfaceRaisedLight,
    onSurfaceVariant = HopTextSecondaryLight,
    surfaceContainer = HopSurfaceLight,
    surfaceContainerHigh = HopSurfaceRaisedLight,
    surfaceContainerHighest = HopSurfaceRaisedLight,
    surfaceContainerLow = HopSurfaceLight,
    surfaceContainerLowest = HopBgLight,
    outline = HopBorderLight,
    outlineVariant = HopBorderLight,
    error = HopDanger,
    onError = HopAccentTextLight,
    scrim = HopScrimLight,
)

@Composable
fun HopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) HopDarkColors else HopLightColors
    val colorScheme = if (darkTheme) hopDarkScheme() else hopLightScheme()
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    CompositionLocalProvider(LocalHopColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = HopShapes,
            content = content,
        )
    }
}
