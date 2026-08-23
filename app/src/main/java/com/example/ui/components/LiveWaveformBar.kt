package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.AkiraState
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraVioletSecondary
import kotlin.random.Random

@Composable
fun LiveWaveformBar(
    state: AkiraState,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 14
    val isLive = state == AkiraState.LISTENING || state == AkiraState.SPEAKING

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val barHeightRatio = remember { Animatable(0.2f) }

            LaunchedEffect(amplitude, isLive) {
                if (isLive) {
                    val multiplier = when (i) {
                        0, barCount - 1 -> 0.4f
                        1, barCount - 2 -> 0.65f
                        2, barCount - 3 -> 0.85f
                        else -> 1.0f
                    }
                    val randomizedNoise = (Random.nextFloat() * 0.35f)
                    val target = ((amplitude * multiplier) + randomizedNoise).coerceIn(0.15f, 1.0f)
                    barHeightRatio.animateTo(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 70, easing = LinearEasing)
                    )
                } else {
                    barHeightRatio.animateTo(
                        targetValue = 0.15f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            }

            val heightDp = (44 * barHeightRatio.value).coerceAtLeast(6f).dp

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(heightDp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            colors = if (isLive) {
                                listOf(AkiraCyanPrimary, AkiraVioletSecondary)
                            } else {
                                listOf(Color(0xFF333B53), Color(0xFF1E2333))
                            }
                        )
                    )
            )
        }
    }
}
