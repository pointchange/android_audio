package com.pointchange.audio.view.component

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointchange.audio.R
import com.pointchange.audio.model.LrcData
import com.pointchange.audio.model.LrcLine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun Lrc(
    lrcContent: List<LrcLine>,
    current: StateFlow<Long>,
    onSeekTo: (current: Long) -> Unit

) {
    val current by current.collectAsStateWithLifecycle()
    val currentIndex = lrcContent.indexOfLast { it.time <= current }.coerceAtLeast(0)
    val listState = rememberLazyListState()
    var isUserOperation by remember { mutableStateOf(false) }
    var columnCenterIndex by remember { mutableIntStateOf(-1) }

    val rowLightIndex by remember(currentIndex) {
        derivedStateOf {
            if (isUserOperation) columnCenterIndex else currentIndex
        }
    }
    val scope = rememberCoroutineScope()
    var syncJob by remember { mutableStateOf<Job?>(null) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect {
                if (isUserOperation) {
                    val middleY = it.viewportEndOffset / 2
                    val closeCenterItem = it.visibleItemsInfo.minByOrNull { item ->
                        val itemCenter = item.offset + (item.size / 2)
                        abs(itemCenter - middleY)
                    }
                    if (closeCenterItem != null) {
                        columnCenterIndex = closeCenterItem.index
                    }

                }
            }
    }
    LaunchedEffect(currentIndex) {
        if (!isUserOperation && currentIndex >= 0) {
//            listState.animateScrollToItem(currentIndex, scrollOffset = -center)
            scrollToCenter(currentIndex, listState)
        }
//        if (lrcContent.isNotEmpty() && !isUserOperation) {
//        }
    }
    val hapticFeedback = LocalHapticFeedback.current
//    LaunchedEffect(columnCenterIndex) {
//        if(isUserOperation &&columnCenterIndex>=0){
//            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//        }
//    }
    val context= LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        }else{
            @Suppress("DEPRECATED")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    LaunchedEffect(columnCenterIndex) {
        if(isUserOperation &&columnCenterIndex>=0){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.4F).compose()
                vibrator.vibrate(effect)
            }
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }else{
            @Suppress("DEPRECATED")
            vibrator.vibrate(10)
        }
    }
    val controlsAlpha by animateFloatAsState(
        targetValue = if (isUserOperation) 1F else 0F,
        animationSpec = tween(durationMillis = 300),
        label = "ControlsAlpha"
    )
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> {
                                    isUserOperation = true
                                    syncJob?.cancel()
                                }

                                PointerEventType.Release -> {
                                    syncJob?.cancel()
                                    syncJob = scope.launch {
                                        delay(2000)
                                        isUserOperation = false
                                        if (currentIndex >= 0) {
                                            scrollToCenter(currentIndex, listState)

                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            contentPadding = PaddingValues(vertical = 100.dp)
        ) {
            itemsIndexed(lrcContent) { index, line ->
                val isSelected = index == rowLightIndex
//            val textColor by animateColorAsState(
//                targetValue =if (isSelected) Color.White else Color.Gray.copy(alpha = 0.6F),
//                animationSpec = tween (durationMillis = 300),
//                label = "LrcTextColor"
//            )
//            val lrcFontSize by animateFloatAsState(
//                targetValue =if (isSelected) 22F else 18F,
//                animationSpec = tween (durationMillis = 300),
//                label = "LrcFontSize"
//            )
                Text(
                    text = line.text,
                    fontSize = if (isSelected) 22.sp else 18.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.6F),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .animateContentSize(),

                    textAlign = TextAlign.Center
                )
            }
        }
        if (controlsAlpha > 0F) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()

                    .graphicsLayer(alpha = controlsAlpha)
            ) {
                //.height(1.dp).padding(horizontal = 60.dp)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 60.dp)
                ) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.3F),
                        start = Offset(0F, 0F),
                        end = Offset(size.width, 0F),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10F, 10F), 0F)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val centerLrc = lrcContent.getOrNull(columnCenterIndex)
                    val timeText = centerLrc?.text ?: ""
                    IconButton(
                        onClick = {
                            centerLrc?.let {
                                onSeekTo(it.time)
                                isUserOperation = false
                                syncJob?.cancel()
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color.White.copy(0.2F),
                                CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.round_play_arrow_24),
                            contentDescription = stringResource(R.string.play)
                        )
                    }
                    Text(text = timeText, color = Color.White.copy(alpha = 0.6F), fontSize = 12.sp)
                }
            }
        }
    }


}

suspend fun scrollToCenter(index: Int, listState: LazyListState) {
    val layoutInfo = listState.layoutInfo
    val h = layoutInfo.viewportEndOffset
    val itemSize = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.size ?: 120
    val centerOffset = -(h / 2 - itemSize / 2)
    listState.animateScrollToItem(index, centerOffset)
}