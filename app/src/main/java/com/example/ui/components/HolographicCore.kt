package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.voice.JarvisState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HolographicCore(
    state: JarvisState,
    rmsLevel: Float,
    glowIntensity: Float = 1.0f,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram")

    // Rotation speeds
    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 12000 else if (state == JarvisState.THINKING) 2500 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotFast"
    )

    val rotationSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reduceMotion) 20000 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotSlow"
    )

    // Pulse & Breathing
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val coreColor = when (state) {
        JarvisState.IDLE -> ElectricCyan
        JarvisState.LISTENING -> ElectricCyan
        JarvisState.THINKING -> TechBlue
        JarvisState.SPEAKING -> PurpleAccent
        JarvisState.EXECUTING -> ElectricCyan
        JarvisState.SUCCESS -> SuccessGreen
        JarvisState.ERROR -> ErrorRed
        JarvisState.SLEEP -> DimText
        JarvisState.PRIVACY_MODE -> WarningAmber
    }

    val dynamicScale = when (state) {
        JarvisState.LISTENING -> 1.0f + (rmsLevel * 0.18f)
        JarvisState.SPEAKING -> 1.0f + (pulseGlow * 0.08f)
        JarvisState.THINKING -> 1.02f
        JarvisState.SLEEP -> 0.92f
        JarvisState.PRIVACY_MODE -> 0.92f
        else -> breathingScale
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .scale(dynamicScale),
        contentAlignment = Alignment.Center
    ) {
        // Holographic Concentric Circles & Arc HUD Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.width / 2

            // Outer subtle radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = 0.25f * glowIntensity),
                        coreColor.copy(alpha = 0.05f * glowIntensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // Outer HUD Segment Ring
            rotate(rotationFast, pivot = center) {
                drawCircle(
                    color = coreColor.copy(alpha = 0.35f),
                    radius = maxRadius - 12.dp.toPx(),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 10f, 15f), 0f)
                    )
                )

                // 4 Orbiting Cyber Pips
                val orbitRadius = maxRadius - 12.dp.toPx()
                for (i in 0 until 4) {
                    val angle = (i * 90) * (Math.PI / 180).toFloat()
                    val pipOffset = Offset(
                        center.x + orbitRadius * cos(angle),
                        center.y + orbitRadius * sin(angle)
                    )
                    drawCircle(
                        color = coreColor,
                        radius = 3.5.dp.toPx(),
                        center = pipOffset
                    )
                }
            }

            // Middle Counter-rotating Ring
            rotate(rotationSlow, pivot = center) {
                drawCircle(
                    color = coreColor.copy(alpha = 0.45f),
                    radius = maxRadius - 28.dp.toPx(),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)
                    )
                )
            }

            // Inner Hologram Ring
            drawCircle(
                color = coreColor.copy(alpha = 0.7f),
                radius = maxRadius - 44.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )

            // Reactive Waveform Equalizer Bars around avatar during Listening or Speaking
            if (state == JarvisState.LISTENING || state == JarvisState.SPEAKING) {
                val barCount = 24
                val barRadius = maxRadius - 44.dp.toPx()
                for (i in 0 until barCount) {
                    val angle = (i * (360f / barCount)) * (Math.PI / 180).toFloat()
                    val waveHeight = if (state == JarvisState.LISTENING) {
                        (10f + (rmsLevel * 35f * (1f + sin(i.toFloat()))))
                    } else {
                        (8f + (pulseGlow * 20f * (1f + cos(i.toFloat()))))
                    }
                    val start = Offset(
                        center.x + barRadius * cos(angle),
                        center.y + barRadius * sin(angle)
                    )
                    val end = Offset(
                        center.x + (barRadius + waveHeight) * cos(angle),
                        center.y + (barRadius + waveHeight) * sin(angle)
                    )
                    drawLine(
                        color = coreColor.copy(alpha = 0.85f),
                        start = start,
                        end = end,
                        strokeWidth = 2.5.dp.toPx()
                    )
                }
            }
        }

        // Central Avatar Core
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SurfaceCardElevated,
                            DeepBackground
                        )
                    )
                )
                .border(2.dp, coreColor.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.jarvis_avatar),
                contentDescription = "JARVIS AI Hologram",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Subtle Cyber Overlay Scanlines
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val scanlineStep = 8.dp.toPx()
                        for (y in 0..(size.height.toInt()) step scanlineStep.toInt()) {
                            drawLine(
                                color = coreColor.copy(alpha = 0.08f),
                                start = Offset(0f, y.toFloat()),
                                end = Offset(size.width, y.toFloat()),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
            )
        }

        // Floating Data Nodes (High Density Design Theme)
        // Top-Right Node: Memory Telemetry
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-8).dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard.copy(alpha = 0.9f))
                .border(0.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "MEMORY",
                    color = SecondaryText,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "4.2 GB / 8 GB",
                    color = ElectricCyan,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom-Left Node: Uplink Sync Telemetry
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-6).dp, y = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard.copy(alpha = 0.9f))
                .border(0.5.dp, TechBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = "SYNC",
                    color = SecondaryText,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "UPLINK ACTIVE",
                    color = SuccessGreen,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Floating State HUD Tag below avatar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 14.dp)
                .background(DeepBackground.copy(alpha = 0.95f), CircleShape)
                .border(1.dp, coreColor.copy(alpha = 0.7f), CircleShape)
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text(
                text = state.label,
                color = coreColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )
        }
    }
}
