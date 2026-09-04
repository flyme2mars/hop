package com.flyme2mars.hop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object HopTokens {
    val Radius4 = 4.dp
    val Radius8 = 8.dp
    val Radius12 = 12.dp
    val Radius16 = 16.dp
    val CardRadius = 20.dp
    val SheetRadius = 28.dp

    val ScreenGutter = 16.dp
    val ScreenGutterWide = 20.dp
    val CardPadding = 16.dp
    val CardPaddingWide = 20.dp
    val ListGap = 12.dp
    val SectionGap = 24.dp
    val Touch = 48.dp
    val ChipMinHeight = 32.dp
    val Avatar = 40.dp
    val SheenHeight = 48.dp
    val FabHaloRadius = 72.dp

    const val RippleAlpha = 0.12f
    const val SheenAlpha = 0.08f
    const val ChipLuminousAlpha = 0.10f
    const val FabHaloAlpha = 0.16f

    const val MotionFast = 150
    const val MotionBase = 200
    const val MotionSlow = 240
    const val ChipSelectMs = 180
    const val PressMs = 100
    const val CutEnterMs = 300
    const val CutClockMs = 350
    const val CandleMs = 2000
    const val PressScale = 0.985f
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
