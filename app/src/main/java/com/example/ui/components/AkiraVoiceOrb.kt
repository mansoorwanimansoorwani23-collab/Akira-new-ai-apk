package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AkiraState
import com.example.ui.theme.AkiraCyanPrimary
import com.example.ui.theme.AkiraErrorCoral
import com.example.ui.theme.AkiraIndigoTertiary
import com.example.ui.theme.AkiraVioletSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AkiraVoiceOrb(
    state: AkiraState,
    amplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbInfiniteTransition")

    // Slow orbital rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == AkiraState.THINKING || state == AkiraState.CONNECTING) 2500 else 10000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    // Smooth amplitude scale animation
    val reactiveScale = remember { Animatable(1f) }
    LaunchedEffect(amplitude, state) {
        val targetScale = when (state) {
            AkiraState.SPEAKING, AkiraState.LISTENING -> 1f + (amplitude * 0.45f)
            AkiraState.THINKING -> 1.05f
            AkiraState.DISCONNECTED -> 0.95f
            else -> 1f
        }
        reactiveScale.animateTo(
            targetValue = targetScale,
            animationSpec = tween(durationMillis = 80, easing = LinearEasing)
        )
    }

    val primaryOrbColor = when (state) {
        AkiraState.LISTENING -> AkiraCyanPrimary
        AkiraState.THINKING -> AkiraVioletSecondary
        AkiraState.SPEAKING -> AkiraCyanPrimary
        AkiraState.ERROR -> AkiraErrorCoral
        AkiraState.DISCONNECTED -> Color(0xFF424A65)
        else -> AkiraIndigoTertiary
    }

    val secondaryOrbColor = when (state) {
        AkiraState.LISTENING -> AkiraVioletSecondary
        AkiraState.THINKING -> Color(0xFFFF4081)
        AkiraState.SPEAKING -> AkiraVioletSecondary
        AkiraState.ERROR -> Color(0xFFFF8A80)
        AkiraState.DISCONNECTED -> Color(0xFF262C42)
        else -> AkiraCyanPrimary
    }

    val glowAlpha = when (state) {
        AkiraState.SPEAKING -> 0.75f + (amplitude * 0.25f)
        AkiraState.LISTENING -> 0.65f + (amplitude * 0.25f)
        AkiraState.THINKING -> 0.70f
        AkiraState.DISCONNECTED -> 0.18f
        else -> 0.45f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(230.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("voice_orb_button")
    ) {
        Canvas(modifier = Modifier.size(230.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = (size.width / 2) * 0.48f * reactiveScale.value * (if (state == AkiraState.DISCONNECTED) 0.95f else idlePulse)

            // 1. Outer Holographic Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryOrbColor.copy(alpha = glowAlpha),
                        secondaryOrbColor.copy(alpha = glowAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.85f
                ),
                center = center,
                radius = baseRadius * 1.85f
            )

            // 2. Rotating Segmented Orbital Rings
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    color = primaryOrbColor.copy(alpha = 0.5f),
                    center = center,
                    radius = baseRadius * 1.35f,
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(35f, 25f, 15f, 25f), 0f)
                    )
                )
            }

            // 3. Counter-Rotating Inner Tech Orbit
            rotate(-rotationAngle * 1.4f, pivot = center) {
                drawCircle(
                    color = secondaryOrbColor.copy(alpha = 0.45f),
                    center = center,
                    radius = baseRadius * 1.18f,
                    style = Stroke(
                        width = 1.8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f), 0f)
                    )
                )

                // 4 Satellite Orbit Nodes
                val nodeAngle = Math.toRadians((rotationAngle * 2).toDouble())
                val nodeOffset = Offset(
                    x = center.x + (cos(nodeAngle).toFloat() * baseRadius * 1.18f),
                    y = center.y + (sin(nodeAngle).toFloat() * baseRadius * 1.18f)
                )
                drawCircle(
                    color = primaryOrbColor,
                    center = nodeOffset,
                    radius = 4.5f
                )
            }

            // 4. Core Holographic Energy Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (state == AkiraState.DISCONNECTED) 0.3f else 0.9f),
                        primaryOrbColor,
                        secondaryOrbColor,
                        Color(0xFF060914)
                    ),
                    center = Offset(center.x - (baseRadius * 0.25f), center.y - (baseRadius * 0.25f)),
                    radius = baseRadius
                ),
                center = center,
                radius = baseRadius
            )

            // 5. Core Rim Specular Ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryOrbColor.copy(alpha = 0.8f),
                        Color.White,
                        secondaryOrbColor.copy(alpha = 0.8f),
                        primaryOrbColor.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                center = center,
                radius = baseRadius,
                style = Stroke(width = 2.5f)
            )
        }

        // Center State Icon for Immediate Visual Clarity
        val centerIcon = when (state) {
            AkiraState.DISCONNECTED -> Icons.Default.PowerSettingsNew
            AkiraState.LISTENING -> Icons.Default.Mic
            AkiraState.SPEAKING -> Icons.Default.VolumeUp
            AkiraState.ERROR -> Icons.Default.Refresh
            else -> Icons.Default.Mic
        }

        Icon(
            imageVector = centerIcon,
            contentDescription = state.label,
            tint = if (state == AkiraState.DISCONNECTED) Color(0xFF8C95B2) else Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}
