package com.pointchange.sampleaudio.ui.component

import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun CircleProgress(
    color: Color
) {
    val viewModel = viewModel<PlayerStateViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentTimeMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    CircularProgressIndicator(
        progress = {
            if (uiState.durationMs == C.TIME_UNSET || uiState.durationMs == 0L) {
                0F
            } else {
                currentTimeMs.toFloat() / uiState.durationMs.toFloat()
            }
        },
        strokeWidth =2.dp,
        color = color
    )
}