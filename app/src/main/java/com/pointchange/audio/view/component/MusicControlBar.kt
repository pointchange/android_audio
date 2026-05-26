package com.pointchange.audio.view.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.PlayList.ARTIST
import com.pointchange.audio.model.PlayList.DEFAULT
import com.pointchange.audio.model.PlayList.FAVORITE
import com.pointchange.audio.model.PlayList.SEARCH
import com.pointchange.audio.model.PlayList.TITLE
import com.pointchange.audio.model.PlayerViewModel
import com.pointchange.audio.model.ThemeConfig
import com.pointchange.audio.model_data.PlayMode.LOOP
import com.pointchange.audio.model_data.PlayMode.RANDOM
import com.pointchange.audio.model_data.PlayMode.REPEAT
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import com.pointchange.audio.view.page.PlayerDragValue
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicControlBar(
    audioListViewModel: AudioListViewModel,
    playerViewModel: PlayerViewModel = viewModel(),
    themeConfig: ThemeConfig,
) {

    val context = LocalContext.current
    val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
    val playingAudioMetadata = playingInfo.audioMetadata
    val title = playingAudioMetadata?.title
        ?: playingAudioMetadata?.uri?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
    val artist = playingAudioMetadata?.artist
        ?: ""
    val current by playerViewModel.current.collectAsStateWithLifecycle()
    val duration by playerViewModel.duration.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    var showClearDialog by remember { mutableStateOf(false) }
    val navActions = LocalNav.current
    val backgroundColor by audioListViewModel.colorFromCover.collectAsStateWithLifecycle()
    ListItem(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(64.dp)
            .clip(shape = RoundedCornerShape(6.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(backgroundColor).copy(alpha = 0.12f),
                        Color(backgroundColor).copy(alpha = 0.4f),
                    )
                )
            )
            .clickable(onClick = { navActions?.add(NavID.Player) }),
        colors = ListItemDefaults.colors(
            containerColor =
                if (playingAudioMetadata?.coverPath != null) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
        ),
        headlineContent = {
            Text(
                text = title,
                fontSize = 20.sp,
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
            )
        },
        supportingContent = {
            if (artist != "") {
                Text(
                    text = artist,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        },
        leadingContent = {
            //Color(0xFF666666)
            Cover(
                modifier = Modifier.size(64.dp),
                context = context,
                audioListViewModel = audioListViewModel,
                themeConfig = themeConfig
            ) {
                audioListViewModel.setColorFromCover(it)
            }
        },
        trailingContent = {
            Row {
                Box(contentAlignment = Alignment.Center) {
                    PlayPauseBtn(
                        isPlaying = isPlaying,
                        onClick = {
                            if (audioListViewModel.playList.isEmpty()) return@PlayPauseBtn
                            if (isPlaying) playerViewModel.pause() else playerViewModel.play()
                        },
                        modifier = Modifier.scale(1.15F)
                    )
                    CircularProgressIndicator(progress = {
                        if (duration == 0L) {
                            0F
                        } else {
                            current / duration.toFloat()
                        }
                    })
                }
                IconButton(onClick = {
                    showBottomSheet = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_format_list_bulleted_24),
                        contentDescription = stringResource(R.string.playing_music_list)
                    )
                }
            }
        },
    )



    if (showBottomSheet) {
        val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
        val list = audioListViewModel.playList
        val lazyListState = rememberLazyListState()
        val reorderableLazyListState =
            rememberReorderableLazyListState(lazyListState) { from, to ->
                audioListViewModel.movePlayListItem(from.index, to.index)

            }
        val text = playingInfo.mode.getString(context)

        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.round_close_24),
                        contentDescription = stringResource(R.string.close),
                        modifier = Modifier.clickable(onClick = { showBottomSheet = false })
                    )
                },
                headlineContent = {},
                trailingContent = {
                    Icon(
                        painter = painterResource(R.drawable.round_delete_sweep_24),
                        contentDescription = stringResource(R.string.clear_play_list),
                        modifier = Modifier.clickable(onClick = { showClearDialog = true })
                    )

                }
            )
            ListItem(leadingContent = {
                Icon(
                    painter = when (playingInfo.currentList) {
                        DEFAULT, TITLE, ARTIST -> painterResource(R.drawable.round_music_note_24)
                        SEARCH -> painterResource(R.drawable.round_search_24)
                        FAVORITE -> painterResource(R.drawable.round_favorite_24)
                    },
                    contentDescription = stringResource(R.string.list),
                )
            }, headlineContent = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = when (playingInfo.currentList) {
                                DEFAULT, TITLE, ARTIST -> stringResource(
                                    R.string.default_list_queue
                                )

                                SEARCH -> stringResource(R.string.search_queue)
                                FAVORITE -> stringResource(R.string.favorite_queue)
                            },
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = " ${list.size} ${stringResource(R.string.song_count)}",
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Text(text = text, style = MaterialTheme.typography.titleMedium)
                }

            }, trailingContent = {
                Icon(
                    painter = painterResource(
                        id = when (playingInfo.mode) {
                            LOOP -> R.drawable.round_repeat_24
                            REPEAT -> R.drawable.round_repeat_one_24
                            RANDOM -> R.drawable.round_shuffle_24
                        }
                    ),
                    contentDescription = text
                )
            })

            LazyColumn(state = lazyListState) {
                items(count = list.size, key = { index -> list[index].uri }) { index ->
                    val item = list[index]
                    val title = item.title ?: File(item.uri).name
                    val color =
                        if (index == playingInfo.index) MaterialTheme.colorScheme.primary else Color.Unspecified

                    ReorderableItem(
                        state = reorderableLazyListState,
                        key = item.uri
                    ) { isDragging ->
                        Box(modifier = Modifier.longPressDraggableHandle()) {
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        painter = painterResource(R.drawable.round_drag_handle_24),
                                        contentDescription = stringResource(R.string.drag),
                                        modifier = Modifier.draggableHandle()
                                    )
                                },
                                headlineContent = {
                                    Text(
                                        text = title,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = color
                                    )
                                },
                                supportingContent = {
                                    if (item.artist != null) {
                                        Text(
                                            text = item.artist,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = color
                                        )
                                    }
                                },
                                trailingContent = {
                                    Icon(
                                        painter = painterResource(R.drawable.round_close_24),
                                        contentDescription = stringResource(R.string.clear_an_item),
                                        modifier = Modifier.clickable(onClick = {

                                            audioListViewModel.removeAtPlayListItem(index).also {
                                                if (audioListViewModel.playList.isEmpty()) {
                                                    playerViewModel.pause()
                                                    audioListViewModel.clearPlayingInfo()
                                                }
                                            }
                                        })
                                    )
                                },
                                modifier = Modifier.clickable(onClick = {
                                    audioListViewModel.playHandle(
                                        item.uri
                                    )
                                })
                            )
                        }
                    }
                }

            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = stringResource(R.string.clear_dialog_content)) },
            confirmButton = {
                TextButton(onClick = {
                    playerViewModel.pause()
                    audioListViewModel.clearPlayList()
                    audioListViewModel.clearPlayingInfo()
                    showClearDialog = false
                }) {
                    Text(
                        text = stringResource(
                            R.string.clear
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearDialog = false
                }) { Text(text = stringResource(R.string.cancel)) }
            },
        )
    }
}
