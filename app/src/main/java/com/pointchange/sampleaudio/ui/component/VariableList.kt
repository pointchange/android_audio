package com.pointchange.sampleaudio.ui.component

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.SongInfoItemQueue
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.utils.handleString
import com.pointchange.sampleaudio.utils.toFormatTime
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariableList(
    currentId: Long,
    color: List<Color>
) {
    val viewModel = viewModel<PlayerStateViewModel>()
    val list = remember { mutableStateListOf<SongInfoItemQueue>() }
    var isOperation by remember { mutableStateOf(false) }
    val queueUiState by viewModel.libraryQueueAsFn.queueUiState.collectAsStateWithLifecycle()
    LaunchedEffect(queueUiState.title, queueUiState.list.size) {
        val result = viewModel.songRepositoryFn.getByIds(queueUiState.list)
        val orderListMap = queueUiState.list.withIndex().associate { it.value to it.index }
            .withDefault { Int.MAX_VALUE }
        val sorted = result.sortedWith(compareBy { orderListMap[it.id] })
        list.clear()
        list.addAll(sorted)
        isOperation = true
    }
    LaunchedEffect(isOperation) {
        if (isOperation) {
            viewModel.libraryQueueAsFn.setQueueStateTitleAndList(
                title = queueUiState.title,
                list = list.map { it.id })
            isOperation = false
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState =
        rememberReorderableLazyListState(lazyListState) { from, to ->
            val temp = list[to.index]
            list[to.index] = list[from.index]
            list[from.index] = temp
            isOperation = true
        }
    val randomColor = listOf(
        Color(0xFFFF0000), // Red
        Color(0xFFFFA500), // Orange
        Color(0xFFFFFF00), // Yellow
        Color(0xFF008000), // Green
        Color(0xFF0000FF), // Blue
        Color(0xFF4B0082), // Indigo
        Color(0xFFEE82EE)  // Violet
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${queueUiState.title} ${list.size}",
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
            items(count = list.size, key = { list[it].path }) { index ->
                val item = list[index]
                val (title, subTitle) = handleString(
                    path = item.path,
                    title = item.title,
                    artist = item.artist,
                    album = item.album
                )

                ReorderableItem(
                    state = reorderableLazyListState,
                    key = item.path
                ) {
                    Box(modifier = Modifier.longPressDraggableHandle()) {
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.prepare(item.id, index) }
                                .background(MaterialTheme.colorScheme.surface)
                                .fillMaxWidth()
                                .padding(
                                    MaterialTheme.ext.spacing.small
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                modifier = Modifier.draggableHandle(),
                                painter = painterResource(R.drawable.baseline_drag_handle_24),
                                contentDescription = stringResource(R.string.drag),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(horizontal = MaterialTheme.ext.spacing.medium)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        modifier = Modifier.weight(1F),
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    AnimatedVisibility(visible = currentId == item.id) {
                                        val labelColor = if (color.isEmpty()) {
                                            Color.Unspecified
                                        } else {
                                            if (color[0].luminance() > 0.55f) {
                                                Color.Black.copy(alpha = 0.87f)
                                            } else {
                                                Color.White.copy(alpha = 0.95f)
                                            }
                                        }
                                        Text(
                                            text = item.path.substringAfterLast(".").uppercase(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontSize = 10.sp,
                                            color = labelColor,
                                            modifier = Modifier
                                                .background(
                                                    color = color.getOrNull(0)
                                                        ?: color.getOrNull(1)
                                                        ?: randomColor[randomColor.indices.random()],
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .padding(
                                                    horizontal = MaterialTheme.ext.spacing.xs
                                                ),

                                            )
                                    }

                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        modifier = Modifier.weight(1F),
                                        text = subTitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = item.durationMs.toFormatTime(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline

                                    )
                                }

                            }
                            Icon(
                                modifier = Modifier.clickable {
                                    list.removeAt(index)
                                    if (item.id == currentId) {
                                        list.getOrNull(0)?.let {
                                            viewModel.prepare(it.id, 0)
                                        }
                                    }
                                    isOperation = true
                                },
                                painter = painterResource(R.drawable.baseline_delete_24),
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }


//                        ListItem(
////                            colors = ListItemDefaults.colors(),
//                            modifier = Modifier.clickable(
//                                onClick = {
//                                    viewModel.prepare(item.id, index)
//                                }),
//                            leadingContent = {
//                                Icon(
//                                    modifier = Modifier.draggableHandle(),
//                                    painter = painterResource(R.drawable.baseline_drag_handle_24),
//                                    contentDescription = stringResource(R.string.drag),
//                                )
//                            },
//                            headlineContent = {
//                                Text(
//                                    text = title,
//                                    style = MaterialTheme.typography.titleMedium,
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis,
//                                )
//                            },
//                            supportingContent = {
//                                Row(
//                                    modifier = Modifier.fillMaxSize(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        modifier = Modifier.weight(8F),
//                                        text = subTitle,
//                                        maxLines = 1,
//                                        overflow = TextOverflow.Ellipsis,
//
//                                        )
//                                    Text(
//                                        modifier = Modifier.weight(2F),
//                                        text = item.durationMs.toFormatTime(),
//                                    )
//                                }
//                            },
//                            trailingContent = {
//                                Icon(
//                                    modifier = Modifier.clickable {
//                                        list.removeAt(index)
//                                        if (index == currentId) {
//                                            list.getOrNull(0)?.let {
//                                                viewModel.prepare(it.id, 0)
//                                            }
//                                        }
//                                        isOperation = true
//                                    },
//                                    painter = painterResource(R.drawable.baseline_delete_24),
//                                    contentDescription = stringResource(R.string.delete),
//                                )
//                            }
//                        )
                    }
                }
            }
        }
    }
}