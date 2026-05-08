package com.farmeasy.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GaugeWidget(
    value: Float,
    maxValue: Float = 100f,
    label: String,
    unit: String = "%",
    gaugeColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp,
    animationDuration: Int = 1200
) {
    var targetValue by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = animationDuration),
        label = "gauge_animation"
    )

    LaunchedEffect(value) {
        targetValue = value
    }

    val sweepAngle = (animatedValue / maxValue) * 240f // 240 degree arc
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size
            val arcSize = Size(
                canvasSize.width - strokeWidth.toPx(),
                canvasSize.height - strokeWidth.toPx()
            )
            val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Value arc
            drawArc(
                color = gaugeColor,
                startAngle = 150f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%.1f".format(animatedValue),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = gaugeColor
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = gaugeColor
            )
        }
    }
}
