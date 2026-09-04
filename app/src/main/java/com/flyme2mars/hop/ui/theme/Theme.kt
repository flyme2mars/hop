package com.flyme2mars.hop.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HopGreen,
    onPrimary = HopInk,
    secondary = HopGreenDim,
    onSecondary = HopMist,
    background = HopInk,
    onBackground = HopMist,
    surface = HopInk,
    onSurface = HopMist,
    onSurfaceVariant = Color(0xFFA8C0B4),
)

private val LightColorScheme = lightColorScheme(
    primary = HopGreenDim,
    onPrimary = Color.White,
    secondary = HopGreen,
    onSecondary = HopInk,
    background = HopSand,
    onBackground = HopInk,
    surface = HopSand,
    onSurface = HopInk,
    onSurfaceVariant = Color(0xFF3D5349),
)

@Composable
fun HopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
