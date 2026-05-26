package com.pointchange.audio.view.page

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.util.copyToClipboard
import com.pointchange.audio.util.getAudioLrcFromContentResolver
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import java.io.File
import kotlin.collections.List

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanLrcPage(
    audioListViewModel: AudioListViewModel = viewModel(),
) {
    val navActions = LocalNav.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lrcList = rememberSaveable { mutableStateListOf<String>() }
    var scanLrcLoading by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        scanLrcLoading = true
        lrcList.clear()
        val res = getAudioLrcFromContentResolver(context)
        lrcList.addAll(res)
        scanLrcLoading = false
    }

    var selected by remember { mutableIntStateOf(-1) }
    val lrcContent = remember { mutableStateListOf<String>() }
    var openAlertDialog by remember { mutableStateOf(false) }
    val clipToast = stringResource(R.string.clip_toast)
    var openDialogLoading by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navActions?.removeLast()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.round_navigate_before_24),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.lrc_select))
                }
            )
        },
        bottomBar = {
            BottomAppBar(actions = {
                TextButton(onClick = {
                    audioListViewModel.selectLrc(lrcList[selected])
                    navActions?.removeLast()
                }) {
                    Text(
                        text = stringResource(
                            R.string.ok
                        )
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        openDialogLoading = true
                        lrcContent.clear()
                        val item = lrcList[selected]
                        val file = File(item)
                        if (file.exists()) {
                            val list = file.source().buffer().readUtf8().lines()
                            lrcContent.addAll(list)
                        }
                        openDialogLoading = false
                    }
                    if (selected == -1) return@IconButton
                    openAlertDialog = true
                }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_file_open_24),
                        contentDescription = stringResource(
                            R.string.lrc_show_text
                        )
                    )
                }
            })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (scanLrcLoading) {
                Row {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            } else {
                LazyColumn {
                    items(count = lrcList.size) { index ->
                        val item = lrcList[index]
                        ListItem(
                            leadingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.round_article_24),
                                    contentDescription = null
                                )
                            },
                            headlineContent = { Text(text = item) },
                            trailingContent = {
                                RadioButton(selected = selected == index, onClick = null)
                            },
                            modifier = Modifier.clickable(onClick = { selected = index })
                        )
                    }
                }
            }
        }
    }

    if (openAlertDialog) {
        if (openDialogLoading) {
            Row {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            AlertDialog(
                title = {
                    Text(
                        text = lrcList[selected].substringAfterLast("/"),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                    )
                },
                text = {
                    LazyColumn() {
                        items(count = lrcContent.size) { index ->
                            val item = lrcContent[index]
                            Text(text = item)
                        }
                    }

                },
                onDismissRequest = {
                    openAlertDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val str = lrcContent.joinToString("\n")
                                copyToClipboard(context, str, clipToast)
                                openAlertDialog = false
                            }
                        }
                    ) {
                        Text(stringResource(R.string.lrc_copy))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { openAlertDialog = false }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}