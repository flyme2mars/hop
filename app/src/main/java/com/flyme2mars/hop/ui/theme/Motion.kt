package com.flyme2mars.hop.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset

data class HopMotion(
    val reduced: Boolean,
) {
    fun <T> fade(): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(HopTokens.MotionBase, easing = FastOutSlowInEasing)

    fun <T> cutEnter(): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(HopTokens.CutEnterMs, easing = FastOutSlowInEasing)

    fun <T> clockFade(): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(HopTokens.CutClockMs, easing = FastOutSlowInEasing)

    fun <T> chipSelect(): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(HopTokens.ChipSelectMs, easing = FastOutSlowInEasing)

    fun <T> press(): FiniteAnimationSpec<T> =
        if (reduced) snap() else tween(HopTokens.PressMs, easing = FastOutSlowInEasing)

    fun <T> fabSpring(): FiniteAnimationSpec<T> =
        if (reduced) {
            snap()
        } else {
            spring(
                dampingRatio = HopTokens.SpringDamping,
                stiffness = Spring.StiffnessMediumLow,
            )
        }

    fun placement(): FiniteAnimationSpec<IntOffset> =
        if (reduced) {
            snap()
        } else {
            spring(
                dampingRatio = HopTokens.SpringDamping,
                stiffness = Spring.StiffnessMediumLow,
            )
        }
}

@Composable
fun rememberHopMotion(): HopMotion {
    val context = LocalContext.current
    return remember(context) {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        )
        HopMotion(reduced = scale == 0f)
    }
}
