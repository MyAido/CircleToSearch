package com.rr.aido.ui.circletosearch.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PulsingSearchLoader(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")

    // 1. Hyper-speed Rotation for the outer ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing), // Much faster rotation (was 1800)
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    // 2. Rapid Pulse for the core
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutLinearInEasing), // Faster pulse (was 800)
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    // Aido Electric Ocean Colors
    val colors = listOf(
        Color(0xFF1E88E5), // MaterialBlue
        Color(0xFF00ACC1), // MaterialCyan
        Color(0xFF5E35B1), // MaterialIndigo
        Color(0xFF26A69A)  // MaterialTeal
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = size.toPx() * 0.12f // Slightly thicker for boldness
            val radius = (size.toPx() - strokeWidth) / 2

            // Draw fast rotating arc segments
            // We draw 4 arcs with a "comet" tail effect by using a brush or just segments
            val arcLength = 60f 
            
            colors.forEachIndexed { index, color ->
                // Add staggered rotation for a "chasing" effect
                val startAngle = rotation + (index * 90f)
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = arcLength,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth, size.toPx() - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // Draw rapid pulsing core dots
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val dotRadius = size.toPx() * 0.1f * scale // Slightly larger dots
            val dotOffset = size.toPx() * 0.22f 
            
            // Inner dots rotating in reverse or static? Let's rotate them in reverse for chaos/speed
            val innerRotationRad = Math.toRadians(-rotation.toDouble() * 0.5) // Half speed reverse
            
            val offset1 = Offset(
                (dotOffset * Math.cos(innerRotationRad)).toFloat(),
                (dotOffset * Math.sin(innerRotationRad)).toFloat()
            )
            val offset2 = Offset( // 90 deg
                (dotOffset * Math.cos(innerRotationRad + Math.PI/2)).toFloat(),
                (dotOffset * Math.sin(innerRotationRad + Math.PI/2)).toFloat()
            )
            val offset3 = Offset( // 180 deg
                (dotOffset * Math.cos(innerRotationRad + Math.PI)).toFloat(),
                (dotOffset * Math.sin(innerRotationRad + Math.PI)).toFloat()
            )
            val offset4 = Offset( // 270 deg
                (dotOffset * Math.cos(innerRotationRad + 3*Math.PI/2)).toFloat(),
                (dotOffset * Math.sin(innerRotationRad + 3*Math.PI/2)).toFloat()
            )

            drawCircle(colors[0], radius = dotRadius, center = center + offset1)
            drawCircle(colors[1], radius = dotRadius, center = center + offset2)
            drawCircle(colors[2], radius = dotRadius, center = center + offset3)
            drawCircle(colors[3], radius = dotRadius, center = center + offset4)
        }
    }
}
