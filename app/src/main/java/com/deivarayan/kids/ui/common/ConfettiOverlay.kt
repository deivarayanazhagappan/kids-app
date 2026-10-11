package com.deivarayan.kids.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private const val PARTICLE_COUNT = 80
private const val FALL_DURATION_MS = 2200

private val confettiColors = listOf(
    Color(0xFFFF7A18),
    Color(0xFF7C4DFF),
    Color(0xFF00C48C),
    Color(0xFFFFC107),
    Color(0xFF1976D2),
    Color(0xFFD32F2F),
)

private data class ConfettiParticle(
    val startXFraction: Float,
    val color: Color,
    val fallDurationMs: Int,
    val delayMs: Int,
    val size: Float,
    val drift: Float,
    val rotationSpeed: Float,
)

/**
 * Bursts confetti from the top of the screen whenever [trigger] changes from
 * its previous value. Pass an ever-increasing counter (e.g. an event count).
 */
@Composable
fun ConfettiOverlay(trigger: Int, modifier: Modifier = Modifier) {
    if (trigger == 0) return

    var visible by remember(trigger) { mutableStateOf(true) }
    val particles = remember(trigger) {
        List(PARTICLE_COUNT) {
            ConfettiParticle(
                startXFraction = Random.nextFloat(),
                color = confettiColors.random(),
                fallDurationMs = Random.nextInt(1200, FALL_DURATION_MS),
                delayMs = Random.nextInt(0, 300),
                size = Random.nextFloat() * 8f + 6f,
                drift = (Random.nextFloat() - 0.5f) * 300f,
                rotationSpeed = Random.nextFloat() * 720f - 360f,
            )
        }
    }
    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(FALL_DURATION_MS, easing = LinearEasing))
        visible = false
    }

    if (!visible) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val elapsedMs = progress.value * FALL_DURATION_MS
        particles.forEach { particle ->
            val localProgress = ((elapsedMs - particle.delayMs) / particle.fallDurationMs).coerceIn(0f, 1f)
            if (localProgress > 0f) {
                val y = localProgress * size.height * 1.1f
                val x = particle.startXFraction * size.width + particle.drift * localProgress
                rotate(degrees = particle.rotationSpeed * localProgress, pivot = Offset(x, y)) {
                    drawRect(
                        color = particle.color,
                        topLeft = Offset(x - particle.size / 2f, y - particle.size / 2f),
                        size = Size(particle.size, particle.size),
                    )
                }
            }
        }
    }
}
