package com.pointchange.sampleaudio.ui.component

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.Route
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel

@ExperimentalMaterial3Api
@OptIn(UnstableApi::class)
@Composable
fun ItemBar() {
    val viewModel: PlayerStateViewModel = viewModel()
    val player by viewModel.controllerFuture.collectAsStateWithLifecycle()
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
//    if (currentState.path.isNullOrEmpty()) {
//        return
//    }
    val title = when {
        currentState.title == null -> ""
        else -> currentState.title.toString()
    }

    val artworkUri = when {
        currentState.coverPath == null -> null
        else -> currentState.coverPath.toString()
    }
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val brushList = if (settingsState.currentPlayCoverColor.isEmpty()) {
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
        )
    } else {
        listOf(
            settingsState.currentPlayCoverColor[0].copy(alpha = 0.9f),
            settingsState.currentPlayCoverColor[1].copy(alpha = 0.7f),
            MaterialTheme.colorScheme.surface
        )
    }
    val titleColor = if (settingsState.currentPlayCoverColor.isEmpty()) {
        Color.Unspecified
    } else {
        if (settingsState.currentPlayCoverColor[2].luminance() > 0.55f) {
            Color.Black.copy(alpha = 0.87f)
        } else {
            Color.White.copy(alpha = 0.95f)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    val swipeThreshold = 72.dp
    val density = LocalDensity.current
    val thresholdPx = with(density) { swipeThreshold.toPx() }
    AnimatedVisibility(visible = !currentState.path.isNullOrEmpty()) {

        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = Modifier
                .pointerInput(thresholdPx) {
                    var totalDrag = 0f
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                        },
                        onDragEnd = {
                            when {
                                totalDrag < -thresholdPx -> viewModel.libraryQueueAsFn.next(player)
                                totalDrag > thresholdPx -> viewModel.libraryQueueAsFn.previous(
                                    player
                                )
                            }
                            totalDrag = 0f
                        },
                        onDragCancel = {
                            totalDrag = 0f
                        }
                    )
                }
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { nav3ViewModel.navigate(Route.Player) })
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        ) {
            Cover(
                artworkUri = artworkUri,
                modifier = Modifier
                    .size(64.dp)
                    .clip(
                        shape = RoundedCornerShape(12.dp)
                    )
                    .zIndex(1F)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp - 8.dp)
                    .background(
                        brush = Brush.horizontalGradient(brushList),
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .zIndex(0F)
                    .border(
                        1.dp,
                        color = Color.Black.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    maxLines = 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp + 8.dp)
                        .basicMarquee()
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircleProgress(
                            if (settingsState.currentPlayCoverColor.isEmpty()) {
                                ProgressIndicatorDefaults.circularDeterminateTrackColor
                            } else {
                                settingsState.currentPlayCoverColor[1]
                            }
                        )
                        PlayPauseButton(player = player)
                    }

                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            painter = painterResource(R.drawable.round_playlist_play_24),
                            contentDescription = stringResource(R.string.current_list),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            VariableList(
                currentId = currentState.id,
                color = settingsState.currentPlayCoverColor
            )
        }
    }
}
