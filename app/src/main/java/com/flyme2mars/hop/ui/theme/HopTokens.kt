package com.flyme2mars.hop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object HopTokens {
    val Radius4 = 4.dp
    val Radius8 = 8.dp
    val Radius12 = 12.dp
    val Radius16 = 16.dp
    val CardRadius = 24.dp
    val SheetRadius = 28.dp
    val LeadingEdge = 3.dp
    val FlameCapWidth = 48.dp
    val FlameCapHeight = 2.dp
    val FlameGlow = 32.dp

    val ScreenGutter = 16.dp
    val ScreenGutterWide = 20.dp
    val CardPadding = 16.dp
    val ListGap = 12.dp
    val SectionGap = 24.dp
    val Touch = 48.dp
    val Avatar = 40.dp
    val CutSlide = 12.dp

    const val ScrimDark = 0.55f
    const val ScrimLight = 0.40f
    const val VignetteDark = 0.15f
    const val VignetteLight = 0.06f
    const val SegmentBrushDark = 0.12f
    const val SegmentBrushLight = 0.07f
    const val SpecularDark = 0.10f
    const val SpecularLight = 0.06f
    const val FlameGlowAlpha = 0.25f

    const val ReducedMs = 80
    const val MotionFast = 150
    const val MotionBase = 200
    const val CutEnterMs = 300
    const val FlameMs = 2400
    const val FloorCutScale = 0.94f
    const val SpringDamping = 0.85f
}

val HopCardShape = RoundedCornerShape(HopTokens.CardRadius)
val HopSheetShape = RoundedCornerShape(
    topStart = HopTokens.SheetRadius,
    topEnd = HopTokens.SheetRadius,
)
val HopPillShape = RoundedCornerShape(percent = 50)
val HopCutCtaShape = RoundedCornerShape(HopTokens.SheetRadius)

val HopShapes = Shapes(
    extraSmall = RoundedCornerShape(HopTokens.Radius4),
    small = RoundedCornerShape(HopTokens.Radius8),
    medium = RoundedCornerShape(HopTokens.Radius12),
    large = RoundedCornerShape(HopTokens.Radius16),
    extraLarge = RoundedCornerShape(HopTokens.CardRadius),
)
