package com.flyme2mars.hop.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val HopBgDark = Color(0xFF12131A)
val HopSurfaceDark = Color(0xFF1A1B24)
val HopSurfaceRaisedDark = Color(0xFF232433)
val HopBorderDark = Color(0xFF2E3040)
val HopTextPrimaryDark = Color(0xFFF2F3F7)
val HopTextSecondaryDark = Color(0xFF9A9CAF)
val HopAccentDark = Color(0xFF8B9CFF)
val HopAccentTextDark = Color(0xFF0E1018)
val HopDanger = Color(0xFFFF7B7B)
val HopBlackoutBg = Color(0xFF000000)
val HopBlackoutAccent = Color(0xFFE8B86D)
val HopGlassFillDark = Color(0xB81A1B24)
val HopGlassEdgeDark = Color(0x14FFFFFF)
val HopScrimDark = Color(0x73000000)

val HopBgLight = Color(0xFFF4F5F8)
val HopSurfaceLight = Color(0xFFFFFFFF)
val HopSurfaceRaisedLight = Color(0xFFECEEF4)
val HopBorderLight = Color(0xFFD8DAE3)
val HopTextPrimaryLight = Color(0xFF12131A)
val HopTextSecondaryLight = Color(0xFF5C5F70)
val HopAccentLight = Color(0xFF4F5FD6)
val HopAccentTextLight = Color(0xFFFFFFFF)
val HopGlassFillLight = Color(0xCCFFFFFF)
val HopGlassEdgeLight = Color(0x0F000000)
val HopScrimLight = Color(0x73000000)

@Immutable
data class HopColors(
    val bg: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val accentText: Color,
    val danger: Color,
    val blackoutBg: Color,
    val blackoutAccent: Color,
    val glassFill: Color,
    val glassEdge: Color,
    val scrim: Color,
)

val HopDarkColors = HopColors(
    bg = HopBgDark,
    surface = HopSurfaceDark,
    surfaceRaised = HopSurfaceRaisedDark,
    border = HopBorderDark,
    textPrimary = HopTextPrimaryDark,
    textSecondary = HopTextSecondaryDark,
    accent = HopAccentDark,
    accentText = HopAccentTextDark,
    danger = HopDanger,
    blackoutBg = HopBlackoutBg,
    blackoutAccent = HopBlackoutAccent,
    glassFill = HopGlassFillDark,
    glassEdge = HopGlassEdgeDark,
    scrim = HopScrimDark,
)

val HopLightColors = HopColors(
    bg = HopBgLight,
    surface = HopSurfaceLight,
    surfaceRaised = HopSurfaceRaisedLight,
    border = HopBorderLight,
    textPrimary = HopTextPrimaryLight,
    textSecondary = HopTextSecondaryLight,
    accent = HopAccentLight,
    accentText = HopAccentTextLight,
    danger = HopDanger,
    blackoutBg = HopBlackoutBg,
    blackoutAccent = HopBlackoutAccent,
    glassFill = HopGlassFillLight,
    glassEdge = HopGlassEdgeLight,
    scrim = HopScrimLight,
)

val LocalHopColors = staticCompositionLocalOf { HopDarkColors }
