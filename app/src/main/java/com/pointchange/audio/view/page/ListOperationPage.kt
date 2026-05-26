package com.pointchange.audio.view.page

import android.content.ClipData
import android.content.Intent
import android.media.AudioMetadata
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.PlayerViewModel
import com.pointchange.audio.util.getUriFromPath
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import com.pointchange.audio.view.page.ListOperation.*
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOperationPage(
    audioListViewModel: AudioListViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val navActions = LocalNav.current
    val playList = audioListViewModel.playList
    val size = playList.size
    val initSelectList = List(size) { _ -> -1 }
    val selectedList = remember {
        mutableStateListOf<Int>().apply {
            addAll(initSelectList)
        }
    }
    var selectedListCount by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    fun initSelectedState() {
        selectedList.clear()
        selectedList.addAll(initSelectList)
        selectedListCount = 0
    }

    val shareText = stringResource(R.string.share_to)
    val shareTitle = stringResource(R.string.share_title)
    val tempAudioList = remember { mutableStateListOf<String>() }
    val scope = rememberCoroutineScope()

    fun checkChangeState(it: Boolean, index: Int) {
        if (it) {
            selectedListCount++
            selectedList[index] = index
            tempAudioList.add(playList[index].uri)
        } else {
            selectedListCount--
            selectedList[index] = -1
            tempAudioList.remove(playList[index].uri)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                navigationIcon = {
                    TextButton(onClick = { navActions?.removeLast()}) {
                        Text(text = stringResource(R.string.cancel))
                    }
                },
                title = {
                    Text(
                        text = if (selectedListCount == 0) stringResource(R.string.select_item)
                        else "$selectedListCount " + stringResource(R.string.list_options_selected),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    TextButton(onClick = {
                        if (selectedListCount == playList.size) {
                            initSelectedState()
                        } else {
                            selectedList.clear()
                            val size = playList.size
                            selectedList.addAll((0..<size).toList())
                            selectedListCount = size
                        }

                    }) {
                        Text(text = stringResource(if (selectedListCount == playList.size) R.string.deselect_all else R.string.select_all))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    entries.forEach {
                        TextButton(onClick = {
                            when (it) {
                                DELETE -> {
                                    showDeleteDialog = true
                                }

                                ADD_TO_THE_PLAY_LIST -> {
                                    showBottomSheet = true
                                }

                                PLAY_THE_NEXT_ONE -> {
                                    audioListViewModel.addListToPlayList(selectedList)
                                    initSelectedState()
                                }

                                SHARE_LOCAL_FILES -> {
                                    val files = tempAudioList.map { File(it) }
                                    val uri = ArrayList(getUriFromPath(context, files))
                                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                        type = "*/*"
                                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                                        val clipText =
                                            ClipData.newRawUri("${files.size} $shareTitle", uri[0])
                                        for (i in 1 until uri.size) {
                                            clipText.addItem(ClipData.Item(uri[i]))
                                        }
                                        clipData = clipText
                                        context.startActivity(Intent.createChooser(this, shareText))
                                    }
                                }
                            }
                        }) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            )
                            {
                                Icon(painter = painterResource(it.icon), contentDescription = null)
                                Text(
                                    text = stringResource(it.text),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
//            ListItem(
//                headlineContent = {
//                    Text(
//                        text = if (selectedListCount == 0) stringResource(R.string.select_item)
//                        else "$selectedListCount " + stringResource(R.string.list_options_selected),
//                        style = MaterialTheme.typography.titleLarge
//                    )
//                }
//            )
            LazyColumn {
                if (playList.isEmpty()) return@LazyColumn
                items(count = playList.size, key = { index -> playList[index].uri }) { index ->
                    val item = playList[index]
                    val title = item.title ?: item.uri
                    val checked = selectedList[index] == index
                    ListItem(
                        headlineContent = {
                            Text(
                                text = title,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            if (item.artist != null) {
                                Text(
                                    text = item.artist,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )
                            }
                        },
                        trailingContent = {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    checkChangeState(it, index)
                                }
                            )
                        },
                        modifier = Modifier.clickable(onClick = {
                            val bool = !checked
                            checkChangeState(bool, index)
                        })
                    )
                }
            }
        }
    }


    var isDeleteLocal by remember { mutableStateOf(false) }
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
                    scope.launch {
                        val isPause = audioListViewModel.remove(
                            selectedList.toList(),
                            isDeleteLocal = isDeleteLocal
                        )
                        if (isPause) {
                            playerViewModel.pause()
                        }
                        initSelectedState()
                        showDeleteDialog = false
                    }
                }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) { Text(stringResource(R.string.cancel)) }
            })
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.fillMaxHeight(),
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            ListItem(
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.round_favorite_24),
                        contentDescription = null
                    )
                },
                headlineContent = { Text(text = stringResource(R.string.favorite)) },
                modifier = Modifier.clickable(onClick = {
                    audioListViewModel.updateFavoriteListState(
                        selectedList,
                        true
                    )
                    initSelectedState()
                    showBottomSheet = false
                })
            )
        }
    }


}
