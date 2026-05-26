package com.pointchange.audio.view.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointchange.audio.util.formatTime
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProgressAndText(
    currentTime: StateFlow<Long>,
    duration: StateFlow<Long>,
    onValueChangeFinished: (userCurrent: Float) -> Unit = {},
    listItemColors: ListItemColors = ListItemDefaults.colors(),
    textColors:Color= Color.Unspecified,
) {
    val duration by duration.collectAsStateWithLifecycle()
    val current by currentTime.collectAsStateWithLifecycle()
    var userCurrent by remember { mutableFloatStateOf(0F) }
    var isUserOperation by remember { mutableStateOf(false) }
    val currentDisplay by remember(current, userCurrent, isUserOperation) {
        derivedStateOf {
            formatTime(if (isUserOperation) userCurrent.toLong() else current)
        }
    }
    val durationDisplay by remember(duration) {
        derivedStateOf {
            formatTime(duration)
        }
    }
    ListItem(
        headlineContent = {
            Slider(
                value = if (isUserOperation) userCurrent else current.toFloat(),
                valueRange = 0F..duration.toFloat(),
                onValueChangeFinished = {
                    isUserOperation = false
                    onValueChangeFinished(userCurrent)
                },
                onValueChange = {
                    userCurrent = it
                    isUserOperation = true
                }
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = currentDisplay, color = textColors)
                Text(text = durationDisplay,color = textColors)
            }
        },
        colors = listItemColors
    )
}