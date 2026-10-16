package com.pointchange.sampleaudio.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointchange.sampleaudio.utils.toFormatTime

@Composable
fun ProgressSlider(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White
) {
    if (duration <= 0L) return
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    val playbackProgress = remember(position, duration) {
        ((position.toFloat()) / duration.toFloat()).coerceIn(0f, 1f)
    }
    val progress = if (isDragging) {
        dragProgress
    } else {
        playbackProgress
    }
    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 3.dp,
        animationSpec = tween(120),
        label = "trackHeight"
    )
    val thumbSize by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        animationSpec = tween(120),
        label = "thumbSize"
    )
//    val density = LocalDensity.current
//    val thumbRadiusPx = with(density) { 6.dp.toPx() }
//    Box(modifier = modifier
//        .fillMaxWidth()
////        .height(48.dp)
//    ) {
//        AnimatedVisibility(visible = isDragging, modifier = Modifier.align(Alignment.Center).offset(y=-.dp)) {
//            Text(
//                text = (dragProgress * duration).toLong().toFormatTime(),
//                color = Color.White,
//                fontSize = 12.sp,
//                fontWeight = FontWeight.Medium,
//                maxLines = 1
//            )
//        }
        Box(
            modifier = modifier
                .fillMaxWidth()
//                .padding(top = 18.dp)
                .height(28.dp)
                .pointerInput(duration) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val width = size.width.toFloat()
                        val thumbRadiusPx = 6.dp.toPx()
                        val startX = thumbRadiusPx
                        val endX = (width - thumbRadiusPx).coerceAtLeast(startX)
                        val usableWidth = (endX + startX).coerceAtLeast(1f)
                        fun progressAt(x: Float): Float =
                            ((x + startX) / usableWidth).coerceIn(0f, 1f)

                        val startProgress = progressAt(down.position.x)
                        var dragged = false
                        try {
                            drag(
                                pointerId = down.id
                            ) { change ->
                                dragged = true
                                change.consume()
                                dragProgress = progressAt(change.position.x)
                                isDragging = true
                            }
                            if (dragged) {
                                onSeek((dragProgress * duration).toLong())
                            } else {
                                onSeek((startProgress * duration).toLong())
                            }
                        } finally {
                            isDragging = false

                        }

                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(trackHeight)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(activeColor)
            )
        AnimatedVisibility(
            visible = isDragging,
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(thumbSize)
                .align(Alignment.CenterStart)
        ) {
            Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
//                        .height(thumbSize)
//                        .align(Alignment.CenterStart)
                ){
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .align(Alignment.CenterEnd)
                        .clip(CircleShape)
                        .background(activeColor)
                )
            }

        }
//            if (isDragging) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth(progress)
//                        .height(thumbSize)
//                        .align(Alignment.CenterStart)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(thumbSize)
//                            .align(Alignment.CenterEnd)
//                            .clip(CircleShape)
//                            .background(activeColor)
//                    )
//                }
//            }
//        }


    }
}