package com.pointchange.audio.view.component

import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.pointchange.audio.view.page.PlayerDragValue
import kotlin.math.roundToInt

@Composable
fun DraggableBarContainer(
    state: AnchoredDraggableState<PlayerDragValue>,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, state.requireOffset().roundToInt()) }
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Vertical,
                overscrollEffect = null
            )
    ) {
        content()
    }
}