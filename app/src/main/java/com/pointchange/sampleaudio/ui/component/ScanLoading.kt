package com.pointchange.sampleaudio.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pointchange.sampleaudio.R

@Composable
fun ScanLoading(
    modifier: Modifier = Modifier,
    cornerColor: Color = Color.White,
    lineColor: Color = Color.White,
    cornerLength: Dp = 28.dp,
    strokeWidth: Dp = 3.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLoading")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ), repeatMode = RepeatMode.Restart
        ),
        label = "scanProgress"
    )
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val corner = cornerLength.toPx()
        val stroke = strokeWidth.toPx()

        drawLine(
            color = cornerColor,
            start = Offset(0f, corner),
            end = Offset(0f, 0f),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(0f, 0f),
            end = Offset(corner, 0f),
            strokeWidth = stroke
        )

        drawLine(
            color = cornerColor,
            start = Offset(width - corner, 0f),
            end = Offset(width, 0f),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(width, 0f),
            end = Offset(width, corner),
            strokeWidth = stroke
        )

        drawLine(
            color = cornerColor,
            start = Offset(0f, height - corner),
            end = Offset(0f, height),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(0f, height),
            end = Offset(corner, height),
            strokeWidth = stroke
        )

        drawLine(
            color = cornerColor,
            start = Offset(width - corner, height),
            end = Offset(width, height),
            strokeWidth = stroke
        )
        drawLine(
            color = cornerColor,
            start = Offset(width, height - corner),
            end = Offset(width, height),
            strokeWidth = stroke
        )
        val scanY = height * progress
//        drawLine(
//            color = lineColor,
//            start = Offset(0f, scanY),
//            end = Offset(width, scanY),
//            strokeWidth = stroke
//        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    lineColor.copy(alpha = 0.15f),
                    lineColor,
                    lineColor.copy(alpha = 0.15f),
                    Color.Transparent,
                ),
                startY = scanY - 20.dp.toPx(),
                endY = scanY + 20.dp.toPx()
            ),
            topLeft = Offset(0f, scanY - 20.dp.toPx()),
            size = Size(width, 40.dp.toPx())
        )
    }

}
