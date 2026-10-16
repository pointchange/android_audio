package com.pointchange.sampleaudio.ui.page

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.SongInfoItemQueue
import com.pointchange.sampleaudio.ui.component.AddPlayListDialog
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.utils.handleString
import com.pointchange.sampleaudio.utils.toFormatTime
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListPage(
    queueName: String,
    queueList: List<Long>,
) {
    val context = LocalContext.current
    val viewModel = viewModel<PlayerStateViewModel>()
    val list = remember { mutableStateListOf<SongInfoItemQueue>() }
    val ins = remember { mutableStateListOf<Long>() }
    var isOperation by remember { mutableStateOf(true) }
    LaunchedEffect(queueName, isOperation) {
        if (isOperation) {
            val result = viewModel.songRepositoryFn.getByIds(queueList)
            list.clear()
            ins.clear()
            list.addAll(result)
        }
        isOperation = false
    }
    val nav3ViewModel = viewModel<Nav3ViewModel>()

    val allSelected by remember(list.size, ins.size) {
        derivedStateOf {
            if (list.size == ins.size && list.isNotEmpty()) {
                1
            } else {
                0
            }
        }
    }

    val allSelectedList = listOf<Pair<Int, () -> Unit>>(
        R.string.select_all to {
            ins.clear()
            ins.addAll(list.map { it.id })
        },
        R.string.cancel_select_all to {
            ins.clear()
        }
    )
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var deletedDialog by remember { mutableStateOf(false) }
    var addPlayListDialog by remember { mutableStateOf(false) }
    fun selectedEmpty(): Boolean =
        if (ins.isEmpty()) {
            Toast.makeText(context, "未选择任意项", Toast.LENGTH_SHORT).show()
            true
        } else {
            false
        }


    val operations = listOf<Triple<Int, Int, () -> Unit>>(
        Triple(R.string.delete, R.drawable.baseline_delete_24) {
            val empty = selectedEmpty()
            if (empty) return@Triple
            deletedDialog = true
        },
        Triple(R.string.add_playlist, R.drawable.baseline_playlist_add_24) {
            val empty = selectedEmpty()
            if (empty) return@Triple
            addPlayListDialog = true
        },
        Triple(R.string.next_play, R.drawable.baseline_queue_play_next_24) {
            val empty = selectedEmpty()
            if (empty) return@Triple
            viewModel.libraryQueueAsFn.updateCurrentList(ins)
            Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
        },
        Triple(R.string.share, R.drawable.baseline_share_24) {
            val empty = selectedEmpty()
            if (empty) return@Triple

            val paths = list.filter { ins.contains(it.id) }.map { it.path }
            val uris = filePathsToUris(context = context, filePaths = paths)
            shareMultipleFiles(context, uris)
        },
    )
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton { nav3ViewModel.pop() }
                },
                title = {
                    Text(
                        text = stringResource(R.string.selected_item) + ": ${ins.size}",
                    )
                },
                actions = {
                    val textBtn = allSelectedList[allSelected]
                    TextButton(
                        onClick = textBtn.second,
                        border = ButtonDefaults.outlinedButtonBorder(allSelected == 1)
                    ) {
                        Text(text = stringResource(textBtn.first))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    operations.forEach { (text, icon, onClick) ->
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable(onClick = onClick)
                                .padding(16.dp)
                                .weight(1f),

                            ) {
                            Icon(painter = painterResource(icon), contentDescription = null)
                            Text(
                                text = stringResource(text),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(count = list.size, key = { list[it].path }) { index ->
                fun checkHandle(id:Long){
                    with(id) {
                        if (ins.contains(this)) {
                            ins.remove(this)
                        } else {
                            ins.add(this)
                        }
                    }
                }
                val item = list[index]
                val (title, subTitle) = handleString(
                    path = item.path,
                    title = item.title,
                    artist = item.artist,
                    album = item.album
                )
                ListItem(
                    modifier = Modifier.clickable(
                        onClick = {
                            checkHandle(item.id)
                        }),
                    headlineContent = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    supportingContent = {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(8F),
                                text = subTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                modifier = Modifier.weight(2F),
                                text = item.durationMs.toFormatTime(),
                            )
                        }
                    },
                    trailingContent = {
                        Checkbox(
                            checked = ins.contains(item.id),
                            onCheckedChange = {
                                checkHandle(item.id)
                            }
                        )
                    }
                )
            }
        }
    }
    if (deletedDialog) {
        var checked by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { deletedDialog = false },
            title = { Text(text = "您确定要删除？") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "永久删除本地存储音频文件")
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val deletedItem = list.filter { ins.contains(it.id) }.map { it.path }
                    viewModel.deleteByIds(ins, deletedItem, checked)
                    isOperation = true
                    deletedDialog = false
                    Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()
                }) { Text(text = stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    deletedDialog = false
                }) { Text(text = stringResource(R.string.cancel)) }
            }
        )
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
                            .apply { addAll(ins) }
                            .distinct()
                    )
                    Toast.makeText(context, "添加成功", Toast.LENGTH_SHORT).show()
                }
            }
        ) { addPlayListDialog = false }
    }
}

fun filePathsToUris(context: Context, filePaths: List<String>): ArrayList<Uri> {
    return ArrayList(filePaths.map { path ->
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(path)
        )
    })
}

fun shareMultipleFiles(context: Context, fileUris: ArrayList<Uri>) {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "audio/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享文件"))
}