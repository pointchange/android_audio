package com.pointchange.sampleaudio.ui.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.SongInfo
import com.pointchange.sampleaudio.utils.handleString
import com.pointchange.sampleaudio.utils.toFormatTime
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LazyColumnList(
    list: List<SongInfo>,
    isShowCover: Boolean = false,
    onDetail: (path: String) -> Unit = {},
    onClickItem: (id: Long, index: Int) -> Unit = { id, index -> },
) {
    val context = LocalContext.current
    val viewModel = viewModel<PlayerStateViewModel>()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    var showSheetState by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<SongInfo?>(null) }
    var addPlayListDialog by remember { mutableStateOf(false) }
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(count = list.size, key = { list[it].path }) { index ->
            val song = list[index]
            val (title, subTitle) = handleString(
                path = song.path,
                title = song.title,
                artist = song.artist,
                album = song.album
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .combinedClickable(
                        onClick = {
                            onClickItem(song.id, index)
                        },
                        onLongClick = {
                            onDetail(song.path)
//                            nav3ViewModel.navigate(Route.Info(song.path))
                        }
                    )
                    .background(
                        color = if(currentState.id==song.id) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                        }else{
                            Color.Transparent
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)

            ) {
                if (isShowCover) {
                    Box(modifier = Modifier.size(60.dp)) {
                        Cover(
                            artworkUri = song.coverPath,
                            modifier = Modifier.clip(shape = RoundedCornerShape(8.dp))
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (isShowCover) 16.dp else 0.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subTitle,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = song.durationMs.toFormatTime(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                IconButton(onClick = {
                    showSheetState = true
                    currentItem = song
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_more_vert_24),
                        contentDescription = stringResource(R.string.more),
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
    }

    if (showSheetState) {
        val sheetList = listOf<Triple<Int, Int, () -> Unit>>(
            Triple(R.string.next_play, R.drawable.baseline_queue_play_next_24) {
                currentItem?.let {
                    viewModel.libraryQueueAsFn.updateCurrentList(listOf(it.id))
                    Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                }
            },
            Triple(R.string.share, R.drawable.baseline_share_24) {
                currentItem?.let {
                    shareOneFile(context, it.path)
                }
            },
            Triple(R.string.add_playlist, R.drawable.baseline_playlist_add_24) {
                addPlayListDialog = true
            }
        )
        ModalBottomSheet(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            onDismissRequest = { showSheetState = false }
        ) {
            LazyColumn {
                items(count = sheetList.size) { index ->
                    val (text, icon, onClick) = sheetList[index]
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onClick() },
                        leadingContent = {
                            Icon(painter = painterResource(icon), contentDescription = null)
                        },
                        headlineContent = { Text(text = stringResource(text)) }
                    )
                }
            }
        }
    }

    if (addPlayListDialog) {
        val queue by viewModel.libraryQueueAsFn.queue.collectAsStateWithLifecycle()
        AddPlayListDialog(
            queue = queue,
            confirmButton = { selected ->
                queue.getOrNull(selected)?.let {
                    viewModel.libraryQueueAsFn.updateQueueList(
                        queueName = it.name,
                        list = it.list.toMutableList()
                            .apply { currentItem?.let { songInfo -> add(songInfo.id) } }
                            .distinct()
                    )
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                }
            }
        ) { addPlayListDialog = false }
    }
}

fun shareOneFile(context: Context, path: String) {
    val fileUris = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        File(path)
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "audio/*"
        putExtra(Intent.EXTRA_STREAM, fileUris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享文件"))
}

//            LazyColumn(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                items(count = list.itemCount, key = list.itemKey { it.path }) { index ->
//                    list[index]?.let { song ->
//                        val (title, subTitle) = handleString(
//                            path = song.path,
//                            title = song.title,
//                            artist = song.artist,
//                            album = song.album
//                        )
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            modifier = Modifier
//                                .combinedClickable(
//                                    onClick = {
//                                        onClickItem(song.id, index)
//                                    },
//                                    onLongClick = {
//                                        nav3ViewModel.navigate(Route.Info(song.path))
//                                    }
//                                )
//                                .padding(horizontal = 16.dp, vertical = 8.dp)
//                        ) {
//                            if (isShowCover) {
//                                Box(modifier = Modifier.size(60.dp)) {
//                                    Cover(
//                                        artworkUri = song.coverPath,
//                                        modifier = Modifier.clip(shape = RoundedCornerShape(8.dp))
//                                    )
//                                }
//                            }
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .padding(start = if (isShowCover) 16.dp else 0.dp)
//                            ) {
//                                Text(
//                                    text = title,
//                                    style = MaterialTheme.typography.titleLarge,
//                                    maxLines = 1,
//                                    modifier = Modifier.basicMarquee()
//                                )
//                                Text(
//                                    text = subTitle,
//                                    maxLines = 1,
//                                    style = MaterialTheme.typography.labelMedium,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                    modifier = Modifier.basicMarquee()
//                                )
//                            }
//                            Text(
//                                text = song.durationMs.toFormatTime(),
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                style = MaterialTheme.typography.labelMedium,
//                            )
//                            IconButton(onClick = {}) {
//                                Icon(
//                                    painter = painterResource(R.drawable.baseline_more_vert_24),
//                                    contentDescription = stringResource(R.string.more),
//                                    tint = MaterialTheme.colorScheme.outline,
//                                )
//                            }
//                        }
//                    }
//                }
//            }