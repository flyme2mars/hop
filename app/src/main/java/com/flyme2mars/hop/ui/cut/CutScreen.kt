package com.flyme2mars.hop.ui.cut

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flyme2mars.hop.HopAppState
import com.flyme2mars.hop.R
import com.flyme2mars.hop.ui.theme.CutBlack
import com.flyme2mars.hop.ui.theme.CutFlame
import com.flyme2mars.hop.ui.theme.CutOkContainer
import com.flyme2mars.hop.ui.theme.CutOkOnContainer
import com.flyme2mars.hop.ui.theme.CutOnBlack
import com.flyme2mars.hop.ui.theme.CutOnBlackMuted
import com.flyme2mars.hop.ui.theme.CutOnFlame
import com.flyme2mars.hop.ui.theme.HopMotion
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
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
        animationSpec = motion.cutCrossfade(),
        label = "cutCrossfade",
    )
    val clockAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = motion.clockFade(),
        label = "cutClockFade",
    )

    val infinite = rememberInfiniteTransition(label = "cutFlame")
    val breathPulse by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cutFlameScale",
    )
    val breath = if (motion.reduced) 1f else breathPulse
    val breathAlpha = if (motion.reduced) 1f else 0.72f + ((breath - 0.94f) / 0.12f) * 0.28f

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
                .padding(horizontal = 24.dp, vertical = 12.dp),
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
                IconButton(onClick = onLeave) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cd_leave_cut),
                        tint = CutOnBlack,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(clockAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Canvas(
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer {
                            scaleX = breath
                            scaleY = breath
                            alpha = breathAlpha
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
                        .padding(bottom = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CutOnBlack,
                    textAlign = TextAlign.Center,
                )
            }

            FilledTonalButton(
                onClick = onOk,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = CutOkContainer,
                    contentColor = CutOkOnContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = stringResource(R.string.cut_ok),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onHelp,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CutFlame,
                    contentColor = CutOnFlame,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sos,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(
                    text = stringResource(R.string.cut_help),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
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
