package com.pointchange.audio.view.page

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.ScanState.ENDING
import com.pointchange.audio.model.ScanState.LOADING
import com.pointchange.audio.model.ScanState.PENDING
import com.pointchange.audio.model.ScanViewModel
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanPage(
    scanViewModel: ScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val scanState by scanViewModel.scanState.collectAsStateWithLifecycle()
    val navActions = LocalNav.current
    var scanDegree by remember { mutableStateOf(ScanDegree.GENERAL) }
    val selectedFolderPath by scanViewModel.selectedFolderPath.collectAsStateWithLifecycle()
    val filterLoading by scanViewModel.filterLoading.collectAsStateWithLifecycle()
    val durationState = rememberTextFieldState("0")
    val sizeState = rememberTextFieldState("0")
    val notSelectList = remember { mutableStateListOf<String>() }

    BackHandler(enabled = selectedFolderPath != null) {
        navActions?.removeLast()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navActions?.removeLast()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.round_navigate_before_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.scan_audio))
                },
                actions = {
                    if (scanState != ENDING && filterLoading) return@TopAppBar
                    TextButton(onClick = { scanViewModel.initScanUIState() }) {
                        Text(
                            text = stringResource(
                                R.string.reset_scan
                            )
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                ListItem(headlineContent = {
                    Button(
                        enabled = scanState != LOADING && !filterLoading,
                        onClick = {
                            if (filterLoading) return@Button
                            when (scanState) {
                                PENDING -> {
                                    scanViewModel.setScanDegree(scanDegree)
                                    scanViewModel.mediumScan(context.applicationContext)
                                }

                                LOADING -> {}
                                ENDING -> {
                                    scanViewModel.saveScanResultList(notSelectList)
                                    navActions?.removeLast()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(id = scanState.text))
                    }
                })

            }
        }) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
//            when (scanState) {
//                PENDING, LOADING -> {
//
//
//                }
//
//                ENDING -> {
//
//                }
//            }


            when (scanState) {
                LOADING,
                PENDING -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                        Icon(
                            painter = painterResource(R.drawable.round_music_note_300),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    ScanDegree.entries.forEach {
                        ListItem(
                            leadingContent = {
                                RadioButton(
                                    selected = scanDegree == it,
                                    onClick = { scanDegree = it }
                                )
                            },
                            headlineContent = { Text(text = stringResource(id = it.text)) },
                            modifier = Modifier.clickable(onClick = { scanDegree = it })
                        )
                    }

                }


                ENDING -> {
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(R.string.file_filter))
                        }
                    )
                    ListItem(
                        headlineContent = {
                            OutlinedTextField(
                                state = durationState,
                                label = { Text(text = stringResource(R.string.filter_duration)) },
                                suffix = { Text(text = "s") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !filterLoading
                            )
                        }
                    )
                    ListItem(
                        headlineContent = {
                            OutlinedTextField(
                                state = sizeState,
                                label = { Text(text = stringResource(R.string.filter_file_size)) },
                                suffix = { Text(text = "KB") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !filterLoading
                            )
                        }
                    )
                    ListItem(
                        headlineContent = {
                            Button(
                                enabled = !filterLoading,
                                onClick = {
                                    if (filterLoading) return@Button
                                    scanViewModel.filterFileList(
                                        minDuration = durationState.text.toString().toLong(),
                                        minFileSize = sizeState.text.toString().toLong(),
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.ok))
                            }
                        }
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        val folderGroups by scanViewModel.folderGroups.collectAsStateWithLifecycle()

                        if (selectedFolderPath == null) {
                            LazyColumn() {
                                items(items = folderGroups.keys.toList()) { fullPath ->
                                    ListItem(
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.twotone_folder_open_24),
                                                contentDescription = null,
                                                tint = Color(0xFFFBC02D)
                                            )
                                        },
                                        headlineContent = {
                                            Text(text = fullPath.substringAfterLast("/"))
                                        },
                                        supportingContent = {
                                            Text(
                                                text = fullPath,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        trailingContent = {
                                            CheckBoxDisplay(onCheckedChange = { checked ->
                                                folderGroups[fullPath]?.let {
                                                    if (it.isNotEmpty()) {
                                                        if (checked) {
                                                            notSelectList.removeAll(it)
                                                        } else {
                                                            notSelectList.addAll(it)
                                                        }
                                                    }
                                                }

                                            })
                                        },
                                        modifier = Modifier.clickable(onClick = {
                                            if (filterLoading) return@clickable
                                            scanViewModel.setSelectedFolderPath(
                                                fullPath
                                            )
                                        })
                                    )
                                }
                            }
                            if (filterLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }


                        } else {
                            LazyColumn() {
                                item {
                                    ListItem(
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.round_navigate_before_24),
                                                contentDescription = stringResource(R.string.back),
                                                modifier = Modifier.clickable(onClick = {
                                                    if (filterLoading) return@clickable
                                                    scanViewModel.setSelectedFolderPath()
                                                })
                                            )
                                        },
                                        headlineContent = {
                                            Text(
                                                text = (selectedFolderPath
                                                    ?: "").substringAfterLast("/"),
                                                maxLines = 1,
                                                overflow = TextOverflow.StartEllipsis
                                            )
                                        }
                                    )

                                }
                                items(
                                    items = folderGroups[selectedFolderPath]
                                        ?: emptyList()
                                ) { fullPath ->
                                    ListItem(
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(R.drawable.round_music_note_24),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        headlineContent = {
                                            Text(text = fullPath.substringAfterLast("/"))
                                        },
                                        supportingContent = {
                                            Text(
                                                text = fullPath,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        trailingContent = {
                                            CheckBoxDisplay(onCheckedChange = {
                                                if (it) {
                                                    notSelectList.remove(fullPath)
                                                } else {
                                                    notSelectList.add(fullPath)
                                                }
                                            })
                                        },
                                        modifier = Modifier.clickable(onClick = {
                                            if (filterLoading) return@clickable
                                        })
                                    )
                                }
                            }
                            if (filterLoading) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckBoxDisplay(onCheckedChange: ((Boolean) -> Unit) = {}) {
    val (checked, setChecked) = remember { mutableStateOf(true) }
    Checkbox(
        checked = checked,
        onCheckedChange = {
            setChecked(it)
            onCheckedChange(it)
        }
    )
}

