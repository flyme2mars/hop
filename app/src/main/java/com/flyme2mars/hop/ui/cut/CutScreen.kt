package com.flyme2mars.hop.ui.cut

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.HopAppState
import com.flyme2mars.hop.R
import com.flyme2mars.hop.ui.theme.CutBlack
import com.flyme2mars.hop.ui.theme.CutFlame
import com.flyme2mars.hop.ui.theme.CutOnBlack
import com.flyme2mars.hop.ui.theme.CutOnBlackMuted
import com.flyme2mars.hop.ui.theme.CutOnFlame
import com.flyme2mars.hop.ui.theme.CutWarmWhite
import com.flyme2mars.hop.ui.theme.HopCutCtaShape
import com.flyme2mars.hop.ui.theme.HopMotion
import com.flyme2mars.hop.ui.theme.HopTokens
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun CutScreen(
    nearbyCount: Int,
    status: String?,
    motion: HopMotion,
    onOk: () -> Unit,
    onHelp: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onLeave)

    var entered by remember { mutableStateOf(motion.reduced) }
    LaunchedEffect(motion.reduced) {
        if (!motion.reduced) {
            entered = true
        }
    }

    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = LocalDateTime.now()
        }
    }

    val screenAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = motion.cutEnter(),
        label = "cutEnter",
    )
    val clockAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = motion.clockFade(),
        label = "cutClockFade",
    )

    val infinite = rememberInfiniteTransition(label = "cutCandle")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = HopTokens.CandleMs, easing = LinearEasing),
        ),
        label = "cutSine",
    )
    val sine = sin(phase.toDouble()).toFloat()
    val breathScale = if (motion.reduced) 1f else 1f + 0.06f * sine
    val bloomAlpha = if (motion.reduced) 0.34f else 0.28f + 0.10f * (sine * 0.5f + 0.5f)

    val timeText = remember(now) { now.format(DateTimeFormatter.ofPattern("HH:mm")) }
    val dateText = remember(now) {
        now.format(DateTimeFormatter.ofPattern("EEEE d MMM", Locale.getDefault()))
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CutBlack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(screenAlpha)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = HopTokens.ScreenGutterWide, vertical = HopTokens.ListGap),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.cut_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = CutOnBlack,
                )
                IconButton(
                    onClick = onLeave,
                    modifier = Modifier.size(HopTokens.Touch),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cd_leave_cut),
                        tint = CutOnBlack,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(clockAlpha),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            if (!motion.reduced) {
                                scaleX = breathScale
                                scaleY = breathScale
                            }
                            alpha = bloomAlpha
                        },
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CutFlame.copy(alpha = 0.55f),
                                Color.Transparent,
                            ),
                        ),
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(HopTokens.ListGap),
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                if (!motion.reduced) {
                                    scaleX = breathScale
                                    scaleY = breathScale
                                }
                            },
                    ) {
                        drawPath(flamePath(size), color = CutFlame)
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.displayLarge,
                        color = CutFlame,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = CutOnBlackMuted,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = pluralStringResource(R.plurals.cut_nearby, nearbyCount, nearbyCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = CutOnBlackMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            if (status != null) {
                Text(
                    text = when (status) {
                        HopAppState.CUT_HELP ->
                            pluralStringResource(R.plurals.cut_sent_help, nearbyCount, nearbyCount)
                        else ->
                            pluralStringResource(R.plurals.cut_sent_ok, nearbyCount, nearbyCount)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = HopTokens.ListGap),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CutOnBlack,
                    textAlign = TextAlign.Center,
                )
            }

            CutGradientButton(
                text = stringResource(R.string.cut_ok),
                icon = { Icon(Icons.Outlined.Check, contentDescription = null, tint = CutOnFlame) },
                onClick = onOk,
            )
            Spacer(Modifier.height(HopTokens.Radius8 + HopTokens.Radius4))
            CutOutlineButton(
                text = stringResource(R.string.cut_help),
                icon = { Icon(Icons.Outlined.Sos, contentDescription = null, tint = Color.White) },
                onClick = onHelp,
            )
            Spacer(Modifier.height(HopTokens.ListGap))
        }
    }
}

@Composable
private fun CutGradientButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HopTokens.Touch)
            .clip(HopCutCtaShape)
            .background(Brush.horizontalGradient(listOf(CutFlame, CutWarmWhite)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = HopTokens.CardPadding, vertical = HopTokens.Radius12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.size(HopTokens.Radius8))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = CutOnFlame)
    }
}

@Composable
private fun CutOutlineButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HopTokens.Touch)
            .clip(HopCutCtaShape)
            .border(width = 1.5.dp, color = Color.White, shape = HopCutCtaShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = HopTokens.CardPadding, vertical = HopTokens.Radius12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        icon()
        Spacer(Modifier.size(HopTokens.Radius8))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

private fun flamePath(size: Size): Path {
    val w = size.width
    val h = size.height
    return Path().apply {
        moveTo(w * 0.50f, h * 0.02f)
        cubicTo(w * 0.72f, h * 0.22f, w * 0.92f, h * 0.42f, w * 0.78f, h * 0.70f)
        cubicTo(w * 0.70f, h * 0.90f, w * 0.30f, h * 0.90f, w * 0.22f, h * 0.70f)
        cubicTo(w * 0.08f, h * 0.42f, w * 0.28f, h * 0.22f, w * 0.50f, h * 0.02f)
        close()
    }
}
