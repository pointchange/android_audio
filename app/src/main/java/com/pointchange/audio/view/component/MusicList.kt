package com.pointchange.audio.view.component

import android.content.ClipData
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.PlayList
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.model_data.PlayingInfo
import com.pointchange.audio.util.getUriFromPath
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavActions
import com.pointchange.audio.view.NavID
import java.io.File
import com.pointchange.audio.view.component.AnAudioOperation.*
import kotlinx.coroutines.launch

data class TemporaryAudioInfo(var index: Int, var audioItem: AudioMetadata?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicList(
    list: LazyPagingItems<AudioMetadata>? = null,
    playList: PlayList = PlayList.DEFAULT,
    viewModel: AudioListViewModel,
    tempList: List<AudioMetadata> = emptyList(),
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    var temporaryAudioInfo by remember {
        mutableStateOf(
            TemporaryAudioInfo(
                index = 0,
                audioItem = null
            )
        )
    }
    val navActions = LocalNav.current
    val context = LocalContext.current
    val shareText = stringResource(R.string.share_to)
    val playingInfo by viewModel.playingInfo.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleteLocal by remember { mutableStateOf(false) }

    val isRefreshing = viewModel.isRefreshing
    val onRefresh = {
        viewModel.refreshList()
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn {
            if (list == null) {
                items(count = tempList.size) { index ->
                    val item = tempList[index]
                    ItemContent(
                        item = item,
                        index = index,
                        currentIndex = playingInfo.index,
                        navActions = navActions,
                        trailingOnclick = {
                            temporaryAudioInfo.run {
                                this.index = index
                                audioItem = item
                            }
                            showBottomSheet = true
                        },
                        listItemOnclick = {
                            viewModel.playHandle(item.uri)
                            viewModel.setCurrentPlayListState(state = playList)
                            viewModel.setPlayingInfo(state = playList, index = index)
                        }
                    )
                }
            } else {
                items(count = list.itemCount, key = list.itemKey { it.uri }) { index ->
                    val item = list[index]
                    if (item != null) {
                        ItemContent(
                            item = item,
                            index = index,
                            currentIndex = playingInfo.index,
                            navActions = navActions,
                            trailingOnclick = {
                                temporaryAudioInfo.run {
                                    this.index = index
                                    audioItem = item
                                }
                                showBottomSheet = true
                            },
                            listItemOnclick = {
                                viewModel.playHandle(item.uri)
                                viewModel.setCurrentPlayListState(state = playList)
                                viewModel.setPlayingInfo(state = playList, index = index)

                            }
                        )
                    }
                }
            }
        }

    }

    if (showBottomSheet) {
        val audioItem = temporaryAudioInfo.audioItem ?: return
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            ListItem(
                headlineContent = {
                    Text(
                        text = "${stringResource(R.string.title)}: ${
                            audioItem.title ?: stringResource(
                                R.string.unknow_artist
                            )
                        }",
                        fontSize = 20.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                }
            )
            ListItem(
                headlineContent = {
                    Text(
                        text = "${stringResource(R.string.artist)}: ${
                            audioItem.artist ?: stringResource(
                                R.string.unknow_artist
                            )
                        }",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            )
            entries.forEach {
                ListItem(
                    headlineContent = { Text(text = stringResource(it.text)) },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = it.icon),
                            contentDescription = stringResource(it.text),
                        )
                    },
                    modifier = Modifier.clickable(onClick = {
                        showBottomSheet = false
                        when (it) {
                            PLAY_THE_NEXT_ONE -> {
                                viewModel.addPlayList(
                                    from = temporaryAudioInfo.index,
                                    item = temporaryAudioInfo.audioItem
                                )
                            }

                            ADD_FAVORITE -> {
                                viewModel.updateFavoriteState()
                            }

                            AUDIO_INFO -> {
                                viewModel.parseAudioInfo(audioItem.uri)
                                navActions?.add(NavID.Information)
                            }

                            SHARE_LOCAL_FILES -> {
                                val file = File(audioItem.uri)
                                val uri =
                                    getUriFromPath(context, file) ?: return@clickable
                                Intent(Intent.ACTION_SEND).apply {
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    clipData = ClipData.newRawUri(file.nameWithoutExtension, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    context.startActivity(Intent.createChooser(this, shareText))
                                }
                            }

                            DELETE -> {
                                showDeleteDialog = true
                            }

                            PARSE_AGAIN -> {
                                viewModel.parseOne(audioItem.uri)
                            }
                        }
                    })
                )
            }
        }
    }
    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = stringResource(R.string.delete_dialog_content))
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = isDeleteLocal,
                        onCheckedChange = { isDeleteLocal = it }
                    )
                    Text(text = stringResource(R.string.delete_dialog_checked))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeOne(
                        temporaryAudioInfo.audioItem?.uri ?: "",
                        isDeleteLocal = isDeleteLocal
                    )

                    showDeleteDialog = false
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.cancel)) }
            })
    }

}

@Composable
fun ItemContent(
    item: AudioMetadata,
    index: Int,
    currentIndex: Int,
    navActions: NavActions?,
    trailingOnclick: () -> Unit = {},
    listItemOnclick: () -> Unit = {},
) {
    val isTitleEmpty = item.title == null
//                if (isTitleEmpty) {
//                    viewModel.parseAudioMetadata(item.uri)
//                }
    val title = if (isTitleEmpty) item.uri.substringAfterLast("/")
        .substringBeforeLast(".") else item.title

    val color =
        if (index == currentIndex) MaterialTheme.colorScheme.primary else Color.Unspecified
    ListItem(
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
                    color = color
                )
            }
        },
        trailingContent = {
            IconButton(
                onClick = {
                    trailingOnclick()

                }
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.round_more_vert_24
                    ),
                    contentDescription = stringResource(R.string.more),
                )
            }
        },
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    listItemOnclick()

                },
                onLongClick = {
                    navActions?.add(NavID.ListOperation)
                }
            )
    )

}