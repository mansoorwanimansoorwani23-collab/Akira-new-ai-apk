package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.AkiraState
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraIndigoTertiary
import com.example.ui.theme.AkiraVioletSecondary
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberObsidian
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AkiraBackground(
    state: AkiraState,
    lowEndMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (lowEndMode) {
        // High-performance static gradient for low-end hardware
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CyberDarkSurface,
                            CyberObsidian,
                            Color(0xFF04060C)
                        )
                    )
                )
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "AkiraBgTransition")
    
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val pulseGlow by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AkiraState.SPEAKING -> 800
                    AkiraState.LISTENING -> 1400
                    AkiraState.THINKING -> 1000
                    else -> 3000
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val auraPrimary = when (state) {
        AkiraState.LISTENING -> AkiraCyanPrimary
        AkiraState.THINKING -> AkiraVioletSecondary
        AkiraState.SPEAKING -> AkiraCyanPrimary
        AkiraState.ERROR -> Color(0xFFFF5252)
        else -> AkiraIndigoTertiary
    }

    val auraSecondary = when (state) {
        AkiraState.LISTENING -> AkiraVioletSecondary
        AkiraState.THINKING -> AkiraCyanPrimary
        AkiraState.SPEAKING -> AkiraVioletSecondary
        AkiraState.ERROR -> Color(0xFFFF8A80)
        else -> Color(0xFF00E5FF)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val radians = Math.toRadians(phase.toDouble()).toFloat()

        // 1. Base dark obsidian void
        drawRect(color = CyberObsidian)

        // 2. Upper flowing cyber glow
        val upperCenter = Offset(
            x = width * 0.5f + (cos(radians) * width * 0.15f),
            y = height * 0.28f + (sin(radians) * height * 0.08f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraPrimary.copy(alpha = pulseGlow * 0.35f),
                    auraSecondary.copy(alpha = pulseGlow * 0.15f),
                    Color.Transparent
                ),
                center = upperCenter,
                radius = width * 0.75f
            ),
            center = upperCenter,
            radius = width * 0.75f
        )

        // 3. Lower secondary ambient pulse
        val lowerCenter = Offset(
            x = width * 0.4f - (sin(radians) * width * 0.12f),
            y = height * 0.78f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraSecondary.copy(alpha = 0.18f),
                    Color.Transparent
                ),
                center = lowerCenter,
                radius = width * 0.65f
            ),
            center = lowerCenter,
            radius = width * 0.65f
        )

        // 4. Subtle cybernetic grid lines
        val step = 80f
        var x = 0f
        while (x < width) {
            drawLine(
                color = Color(0x0800E5FF),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += step
        }
        var y = 0f
        while (y < height) {
            drawLine(
                color = Color(0x0800E5FF),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}
