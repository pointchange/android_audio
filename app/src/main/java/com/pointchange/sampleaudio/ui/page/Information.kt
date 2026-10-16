package com.pointchange.sampleaudio.ui.page

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.AudioInfo
import com.pointchange.sampleaudio.data.toInfoItems
import com.pointchange.sampleaudio.ui.component.Cover
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun Information(path: String) {
    val context = LocalContext.current
    val viewModel = viewModel<PlayerStateViewModel>()
    var info by remember { mutableStateOf<AudioInfo?>(null) }
    val artworkUri = remember(info) { info?.artworkUri }
    LaunchedEffect(Unit) {
        info = viewModel.parseOne(path)
    }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Cover(
                modifier = Modifier
                    .combinedClickable(onClick = {}, onLongClick = {
                        artworkUri?.let {
                            showDialog = true
                        }
                    })
                    .fillMaxWidth()
                    .aspectRatio(1F), artworkUri = artworkUri
            )

            info?.let { audioInfo ->
                audioInfo.toInfoItems().forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .combinedClickable(onClick = {}, onLongClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                item.label, item.value
                                            )
                                        )
                                    )
                                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                                }
                            })
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = item.label + " : ", modifier = Modifier.weight(2F))
                        Text(
                            text = "${item.value}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(8F)
                        )
                    }
                }
            }
        }

        FilledTonalIconButton(
            onClick = { nav3ViewModel.pop() },
            modifier = Modifier
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    start = 16.dp
                )
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_arrow_back_24),
                contentDescription = stringResource(R.string.back),
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(text = "提取封面？")
            },
            confirmButton = {
                Button(onClick = {
                    artworkUri?.toUri()?.path?.let {
                        viewModel.saveInPublicPicture(context = context, coverPath = it)
                    }
                    showDialog = false
                }) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}