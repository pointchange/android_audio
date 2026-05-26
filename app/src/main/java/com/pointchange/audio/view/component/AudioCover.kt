package com.pointchange.audio.view.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AudioCover(
    remainColor: Color = Color(0xFFCCCCCC),
    mostColor: Color = Color(0xFFEEEEEE),
    sizeInt: Dp = 400.dp,
) {
    Box(modifier = Modifier.size(sizeInt)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = mostColor,
                size = size,
                cornerRadius = CornerRadius(size.width / 7.5F)
            )

//            val xy = w/2
            val xy = size.width / 5.20F
            val r1 = xy * 0.8
            drawCircle(
                color = remainColor,
                radius = r1.dp.toPx()
            )
            val r2 = xy * 0.25
            drawCircle(
                color = mostColor,
                radius = r2.dp.toPx()
            )
            translate(left = size.width / 1.8F) {
                drawRect(
                    color = mostColor,
                    size = Size(size.width / 14F, size.height / 2F)
                )
            }

        }
    }
}
