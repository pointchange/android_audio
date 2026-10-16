package com.pointchange.sampleaudio.ui.page

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.player.lyrics.LyricsUiState
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.ui.component.ColumnCenterContainer
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsInformationPage() {
    val viewModel = viewModel<PlayerStateViewModel>()
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val lyricsUiState by viewModel.lyricsUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    var content by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (lyricsUiState is LyricsUiState.Success) {
            val document = (lyricsUiState as LyricsUiState.Success).document
            val file = File(document.path)
            if(file.exists()){
                content=file.readText()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton { nav3ViewModel.pop() } },
                title = {
                    if (lyricsUiState is LyricsUiState.Success) {
                        Text(
                            text = (lyricsUiState as LyricsUiState.Success).document.path.substringAfterLast(
                                "/"
                            ).substringBefore("."),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(),
                        )

                    }
                },
                actions = {
                    if (lyricsUiState is LyricsUiState.Success) {
                        val document = (lyricsUiState as LyricsUiState.Success).document

                        IconButton(onClick = {
                            scope.launch {
                                if (content.isNotEmpty()) {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText(
                                                document.path, content
                                            )
                                        )
                                    )
                                }
                                Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                painterResource(R.drawable.baseline_copy_all_24),
                                contentDescription = stringResource(R.string.copy_all)
                            )
                        }
                    }

                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(MaterialTheme.ext.spacing.medium)
                .fillMaxSize()
        ) {

            when (lyricsUiState) {
                LyricsUiState.Empty -> {
                    ColumnCenterContainer {
                        Text(
                            text = stringResource(R.string.empty_data),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                is LyricsUiState.Error -> {
                    ColumnCenterContainer {
                        Text(
                            text = stringResource(R.string.error),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                LyricsUiState.Loading -> {
                    ColumnCenterContainer {
                        CircularProgressIndicator()
                    }
                }

                is LyricsUiState.Success -> {
                    Text(text = content, modifier = Modifier.verticalScroll(rememberScrollState()))
                }
            }
        }

    }

//    Scaffold(
//        topBar = {
//            TopAppBar(
//                navigationIcon = { BackButton { nav3ViewModel.pop() } },
//                title = {
//                    if (lyricsUiState is LyricsUiState.Success) {
//                        Text(
//                            text = (lyricsUiState as LyricsUiState.Success).document.path.substringAfterLast(
//                                "/"
//                            ).substringBefore(".")
//                        )
//
//                    }
//                },
//                actions = {
//                    if (lyricsUiState is LyricsUiState.Success) {
//                        val document = (lyricsUiState as LyricsUiState.Success).document
//                        IconButton(onClick = {
//                            scope.launch {
//                                val file = File(document.path)
//                                if (file.exists()) {
//                                    clipboard.setClipEntry(
//                                        ClipEntry(
//                                            ClipData.newPlainText(
//                                                file.absolutePath, file.readText()
//                                            )
//                                        )
//                                    )
//                                }
//                                Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
//                            }
//                        }) {
//                            Icon(
//                                painterResource(R.drawable.baseline_copy_all_24),
//                                contentDescription = stringResource(R.string.copy_all)
//                            )
//                        }
//                    }
//
//                }
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .padding(paddingValues)
//                .fillMaxSize()
//        ) {
//
//            when (lyricsUiState) {
//                LyricsUiState.Empty -> {
//                    ColumnCenterContainer {
//                        Text(
//                            text = stringResource(R.string.empty_data),
//                            style = MaterialTheme.typography.displayMedium,
//                            color = MaterialTheme.colorScheme.surfaceVariant
//                        )
//                    }
//                }
//
//                is LyricsUiState.Error -> {
//                    ColumnCenterContainer {
//                        Text(
//                            text = stringResource(R.string.error),
//                            style = MaterialTheme.typography.displayMedium,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//
//                LyricsUiState.Loading -> {
//                    ColumnCenterContainer {
//                        CircularProgressIndicator()
//                    }
//                }
//
//                is LyricsUiState.Success -> {
//                    val lyricsDocument = (lyricsUiState as LyricsUiState.Success).document.line
//                    LazyColumn(modifier = Modifier.fillMaxSize()) {
//                        items(count = lyricsDocument.size) { index ->
//                            val item = lyricsDocument[index]
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .matchParentSize()
//                                    .padding(
//                                        horizontal = MaterialTheme.ext.spacing.medium,
//                                        vertical = MaterialTheme.ext.spacing.small
//                                    ),
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
//                            ) {
//                                Text(text = item.startTimeMs.toFormatTime() + " " + item.text)
//                                IconButton(onClick = {
//                                    scope.launch {
//                                        clipboard.setClipEntry(
//                                            ClipEntry(
//                                                ClipData.newPlainText(
//                                                    item.text + Random.nextInt(), item.text
//                                                )
//                                            )
//                                        )
//                                    }
//                                    Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
//                                }) {
//                                    Icon(
//                                        painter = painterResource(R.drawable.baseline_content_copy_24),
//                                        contentDescription = stringResource(R.string.copy)
//                                    )
//                                }
//                            }
//
//                        }
//                    }
//
//                }
//            }
//        }
//
//    }
}

