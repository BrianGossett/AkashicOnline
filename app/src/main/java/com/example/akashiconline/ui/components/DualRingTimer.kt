package com.example.akashiconline.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DualRingTimer(
    outerProgress: Float,
    innerProgress: Float,
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val outerColor = Color(0xFFAFA9EC)
    val innerColor = Color(0xFF3C3489)
    val trackColor = outerColor.copy(alpha = 0.2f)
    val innerTrackColor = innerColor.copy(alpha = 0.15f)

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.width * 0.08f
            val innerStrokeWidth = size.width * 0.07f
            val padding = strokeWidth / 2f
            val innerPadding = padding + strokeWidth + innerStrokeWidth / 2f + 6.dp.toPx()

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = Size(size.width - padding * 2, size.height - padding * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            if (outerProgress > 0f) {
                drawArc(
                    color = outerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * outerProgress,
                    useCenter = false,
                    topLeft = Offset(padding, padding),
                    size = Size(size.width - padding * 2, size.height - padding * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }

            drawArc(
                color = innerTrackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(innerPadding, innerPadding),
                size = Size(size.width - innerPadding * 2, size.height - innerPadding * 2),
                style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round),
            )
            if (innerProgress > 0f) {
                drawArc(
                    color = innerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * innerProgress,
                    useCenter = false,
                    topLeft = Offset(innerPadding, innerPadding),
                    size = Size(size.width - innerPadding * 2, size.height - innerPadding * 2),
                    style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatMmSs(elapsedSeconds),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
            )
            Text(
                text = "elapsed",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatMmSs(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
