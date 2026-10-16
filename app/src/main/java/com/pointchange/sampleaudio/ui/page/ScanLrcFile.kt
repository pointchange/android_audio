package com.pointchange.sampleaudio.ui.page

import android.content.ClipData
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.AudioGroup
import com.pointchange.sampleaudio.data.LoadLyricsState
import com.pointchange.sampleaudio.data.ScanLyricsUiState
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.ui.component.LazyScanResultList
import com.pointchange.sampleaudio.ui.component.LyricsItemTextDialog
import com.pointchange.sampleaudio.ui.component.ScanLoading
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Paths
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanLrcFile() {
    val context = LocalContext.current
    val viewModel = viewModel<PlayerStateViewModel>()
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val scanLyricsUiState: ScanLyricsUiState by viewModel.scanLyricsUiState.collectAsStateWithLifecycle()
    val bottomText = when (scanLyricsUiState.state) {
        LoadLyricsState.PREPARE -> {
            R.string.scan_lyrics_file_start to {
                viewModel.scanLyricsFile(true)
            }
        }

        LoadLyricsState.LOADING -> {
            R.string.cancel to {
                viewModel.cancelScanLyrics()
            }
        }

        LoadLyricsState.END -> {
            R.string.ok to {
                viewModel.clearScanLyricsUiState()
            }
        }
    }
    var showLyricsItemTextDialog by remember { mutableStateOf(false) }
    var lyricsItemTextPath by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton { nav3ViewModel.pop() }
                },
                title = {
                    Text(text = stringResource(R.string.scan_all_lyrics_file))

                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = bottomText.second
                ) {
                    Text(text = stringResource(bottomText.first))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(MaterialTheme.ext.spacing.medium)
                .fillMaxSize()
        ) {
            when (scanLyricsUiState.state) {
                LoadLyricsState.PREPARE -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "目前仅支持 .lrc 文件")
                    }
                }

                LoadLyricsState.LOADING -> {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(R.drawable.round_music_note_24),
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                        ScanLoading(
                            modifier = Modifier.fillMaxSize(),
                            cornerColor = MaterialTheme.colorScheme.onSurface,
                            lineColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                LoadLyricsState.END -> {
                    LazyScanResultList(
                        list = scanLyricsUiState.result,
                        onItemClick = {
                            lyricsItemTextPath = it
                            showLyricsItemTextDialog = true
                        })
                    Toast.makeText(context, "已自动更新歌词数据库", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    if (showLyricsItemTextDialog) {
        LyricsItemTextDialog(lyricsItemTextPath){ showLyricsItemTextDialog = false}
//        val text = File(lyricsItemTextPath)
//        if (text.exists()) {
//            val content = text.readText()
//            AlertDialog(
//                onDismissRequest = { showLyricsItemTextDialog = false },
//                title = {
//                    Text(text.nameWithoutExtension)
//                },
//                text = {
//                    Text(
//                        text = content,
//                        modifier = Modifier.verticalScroll(state = rememberScrollState())
//                    )
//                },
//                confirmButton = {
//                    TextButton(onClick = {
//                        scope.launch {
//                            clipboard.setClipEntry(
//                                ClipEntry(
//                                    ClipData.newPlainText(
//                                        text.absolutePath, content
//                                    )
//                                )
//                            )
//                        }
//                        Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
//                        showLyricsItemTextDialog = false
//                    }) { Text(stringResource(R.string.copy)) }
//                },
//                dismissButton = {
//                    TextButton(onClick = {
//                        showLyricsItemTextDialog = false
//                    }) { Text(stringResource(R.string.cancel)) }
//                }
//            )
//        } else {
//            AlertDialog(
//                onDismissRequest = { showLyricsItemTextDialog = false },
//                title = {
//                    Text(
//                        text = stringResource(R.string.scan_lyrics_file_error),
//                        color = MaterialTheme.colorScheme.error
//                    )
//                },
//                confirmButton = {
//                    TextButton(onClick = {
//                        showLyricsItemTextDialog = false
//                    }) { Text(stringResource(R.string.ok)) }
//                },
//                dismissButton = {
//                    TextButton(onClick = {
//                        showLyricsItemTextDialog = false
//                    }) { Text(stringResource(R.string.cancel)) }
//                }
//            )
//        }

    }
}