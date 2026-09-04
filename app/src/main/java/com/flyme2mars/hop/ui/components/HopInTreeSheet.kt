package com.flyme2mars.hop.ui.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopSheetShape
import com.flyme2mars.hop.ui.theme.HopTokens
import com.flyme2mars.hop.ui.theme.InkDarkBg
import kotlinx.coroutines.CancellationException

@Composable
fun HopInTreeSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    motion: HopMotion,
    modifier: Modifier = Modifier,
    sheetModifier: @Composable AnimatedVisibilityScope.() -> Modifier = { Modifier },
    content: @Composable ColumnScope.(AnimatedVisibilityScope) -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val scrim = if (dark) {
        Color.Black.copy(alpha = HopTokens.ScrimDark)
    } else {
        InkDarkBg.copy(alpha = HopTokens.ScrimLight)
    }
    var backProgress by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(backProgress, label = "sheetBack")

    PredictiveBackHandler(enabled = visible) { events ->
        try {
            events.collect { event ->
                backProgress = event.progress
            }
            backProgress = 0f
            onDismiss()
        } catch (_: CancellationException) {
            backProgress = 0f
        }
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.fillMaxSize(),
        enter = if (motion.reduced) {
            fadeIn(motion.fade())
        } else {
            fadeIn(motion.fade()) + slideInVertically(motion.fabSpring()) { it / 2 }
        },
        exit = if (motion.reduced) {
            fadeOut(motion.fade())
        } else {
            fadeOut(motion.fade()) + slideOutVertically(motion.fabSpring()) { it / 2 }
        },
        label = "inTreeSheet",
    ) {
        val sheetAv = this
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrim)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = progress * 120f
                        alpha = 1f - progress * 0.25f
                    }
                    .then(sheetAv.sheetModifier()),
                shape = HopSheetShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = HopTokens.ScreenGutterWide)
                        .padding(bottom = HopTokens.SheetRadius),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = HopTokens.ListGap)
                            .width(32.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(2.dp),
                            ),
                    )
                    content(sheetAv)
                }
            }
        }
    }
}
