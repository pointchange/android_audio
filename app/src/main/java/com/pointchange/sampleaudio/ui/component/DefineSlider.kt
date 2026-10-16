package com.pointchange.sampleaudio.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.utils.toFormatTime
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefineSlider(
    activeTrackColor: Color = MaterialTheme.colorScheme.onSurface,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val viewModel = viewModel<PlayerStateViewModel>()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val player by viewModel.controllerFuture.collectAsStateWithLifecycle()
    player?.let { player ->
        val interactionSource = remember { MutableInteractionSource() }
        val isDragging by interactionSource.collectIsDraggedAsState()

        var uiCurrentTimeMs: Float by remember { mutableFloatStateOf(0f) }
        val duration = when (player.duration) {
            C.TIME_UNSET -> Long.MAX_VALUE
            else -> player.duration
        }
        Box(modifier = Modifier.height(48.dp)) {
            Slider(
                value = if (isDragging) uiCurrentTimeMs else currentTimeMs.toFloat(),
                onValueChange = {
                    uiCurrentTimeMs = it
                },
                onValueChangeFinished = {
                    viewModel.seekTo(uiCurrentTimeMs.toLong())
                },
                valueRange = 0f..(duration.toFloat()),
                modifier = Modifier.height(32.dp),
                interactionSource = interactionSource,
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = activeTrackColor.copy(alpha = if (isDragging) 1f else 0.12f),
                                shape = CircleShape
                            )
                    )
                },
                track = { state ->
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    ) {
                        val centerY = size.height / 2
                        val r = size.height / 2
                        val fraction = state.coercedValueAsFraction
                        drawRoundRect(
                            color = inactiveTrackColor,
                            topLeft = Offset(0f, -centerY),
                            size = size,
                            cornerRadius = CornerRadius(r)
                        )
                        drawRoundRect(
                            color = activeTrackColor,
                            topLeft = Offset(0f, -centerY),
                            size = Size(
                                width = size.width * fraction,
                                height = size.height
                            ),
                            cornerRadius = CornerRadius(r)
                        )
                    }
                }
            )
            AnimatedVisibility(
                visible = isDragging,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        translationY = -120f
                    }
            ) {
                Text(
                    text = uiCurrentTimeMs.toLong().toFormatTime(),
                    style = MaterialTheme.typography.titleLarge,
                    color = activeTrackColor,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(
                            MaterialTheme.ext.spacing.small,
                        )
                )
            }
        }
    }


}