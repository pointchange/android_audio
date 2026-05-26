package com.pointchange.audio.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.util.changeColorDeepOrLight
import com.pointchange.audio.view.page.PlayerDragValue
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SimplePlayList(
    list: SnapshotStateList<AudioMetadata>,
    playListAlpha: Float,
    backgroundColor: Long,
    playerAndPlayListDraggableState: AnchoredDraggableState<PlayerDragValue>,
    onPlay: (uri: String) -> Unit,
    movePlayListItem: (from: Int, to: Int) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            movePlayListItem(from.index, to.index)
        }
    val newBackgroundColor = changeColorDeepOrLight(
        rawColorInt = backgroundColor.toInt(),
        saturability = 0.8F,
        brightness = 0.3F
    ).toArgb()
        .toLong()

    val textAndIconColor = Color.White
    val scope= rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.padding(top = 64.dp),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scope.launch { playerAndPlayListDraggableState.animateTo(PlayerDragValue.COLLAPSED)  } },
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_keyboard_arrow_down_24),
                    contentDescription = stringResource(R.string.back),
                    tint = textAndIconColor
                )
            }
        }
    ) {paddingValues ->

    Box(
        modifier = Modifier.fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
            .background(color = Color(newBackgroundColor).copy(alpha = playListAlpha), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        LazyColumn(state = lazyListState,    modifier = Modifier.padding(bottom = 64.dp)) {
            items(count = list.size, key = { index -> list[index].uri }) { index ->
                val item = list[index]
                val title = item.title ?: item.uri
                ReorderableItem(
                    state = reorderableLazyListState,
                    key = item.uri
                ) { isDragging ->
                    Box(modifier = Modifier.longPressDraggableHandle()) {
                        ListItem(
                            modifier = Modifier.clickable(onClick = { onPlay(item.uri) }),
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            leadingContent = {
                                Box(modifier = Modifier.size(50.dp)) {
                                    if (item.coverPath != null) {
                                        AsyncImage(
                                            model = item.coverPath,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .clip(shape = RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Box(modifier = Modifier.alpha(0.4F)){
                                            AudioCover(
                                                mostColor = Color.White,
                                                remainColor = Color(newBackgroundColor)
                                            )
                                        }
                                    }
                                }
                            },
                            headlineContent = {
                                Text(
                                    text = title,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textAndIconColor
                                )
                            },
                            supportingContent = {
                                if (item.artist != null) {
                                    Text(
                                        text = item.artist,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = textAndIconColor
                                    )
                                }
                            },
                            trailingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.round_drag_handle_24),
                                    contentDescription = stringResource(R.string.drag),
                                    modifier = Modifier.draggableHandle(),
                                    tint = textAndIconColor
                                )
                            }
                        )
                    }
                }

            }
        }
    }
    }

}
