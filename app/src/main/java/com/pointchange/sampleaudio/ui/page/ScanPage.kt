package com.pointchange.sampleaudio.ui.page

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.AudioGroup
import com.pointchange.sampleaudio.data.FULL_SCAN_EXTENSIONS
import com.pointchange.sampleaudio.data.Route
import com.pointchange.sampleaudio.data.ScanConfig
import com.pointchange.sampleaudio.data.ScanState
import com.pointchange.sampleaudio.data.ScanType
import com.pointchange.sampleaudio.remember.rememberUserPreferences
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.ui.component.ColumnCenterContainer
import com.pointchange.sampleaudio.ui.component.LazyScanResultList
import com.pointchange.sampleaudio.ui.component.ScanLoading
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.utils.defineSubList
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths



//fun groundAudioPaths(paths:List<String>):List<AudioGroup>{
//    return paths
//        .asSequence()
//        .map{it.trim()}
//        .filter{it.isNotEmpty()}
//        .map{it.replace('\\','/')}
//        .map{}
//}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFlexBoxApi::class)
@Composable
fun ScanPage() {
    val viewModel = viewModel<PlayerStateViewModel>()
    val scope = rememberCoroutineScope()
    val scanConfig by viewModel.scanConfig.collectAsStateWithLifecycle()
    val scanState by viewModel.scanUiState.collectAsStateWithLifecycle()
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val space = MaterialTheme.ext.spacing.small
    var showEditFormatDialog by remember { mutableStateOf(false) }
    val list = remember { scanConfig.extensions.toMutableStateList() }
    var durationValue by remember { mutableStateOf((scanConfig.minDurationMs / 1000).toString()) }
    var fileSizeValue by remember { mutableStateOf((scanConfig.minFileSize / 1024).toString()) }
    val context = LocalContext.current
    val bottomText = when (scanState.state) {
        ScanState.CONFIG_PREPARE -> R.string.ok to {
            scope.launch {
                val currentScanConfig = ScanConfig(
                    type = ScanType.ALL_FILE,
                    minDurationMs = durationValue.toLong() * 1000L,
                    excludeFolders = scanConfig.excludeFolders,
                    minFileSize = fileSizeValue.toLong() * 1024L,
                    extensions = list.toSet()
                )
                viewModel.setScanConfig(currentScanConfig)

            }
            viewModel.scanAllFile(context = context)
        }

        ScanState.START,
        ScanState.SCANNING,
        ScanState.DURATION,
        ScanState.FILTER,
        ScanState.RESULT -> R.string.cancel to {
            viewModel.cancelScan()
            nav3ViewModel.pop()
        }

        ScanState.CANCELLED, ScanState.ERROR -> R.string.back to { nav3ViewModel.pop() }
        ScanState.END -> R.string.complete to {
            viewModel.diffAndSaveInDatabase(context)
            nav3ViewModel.pop()
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton { nav3ViewModel.pop() }

                },
                title = {
                    Text(text = stringResource(R.string.scan_all_file))
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
            var showDeleteFormatOperation by remember { mutableStateOf(false) }
            val textPadding = if (showDeleteFormatOperation) {
                Modifier.padding(
                    start = MaterialTheme.ext.spacing.xs,
                    top = MaterialTheme.ext.spacing.small,
                    bottom = MaterialTheme.ext.spacing.small,
                    end = MaterialTheme.ext.spacing.medium * 2 - MaterialTheme.ext.spacing.xs
                )
            } else {
                Modifier.padding(
                    vertical = MaterialTheme.ext.spacing.small,
                    horizontal = MaterialTheme.ext.spacing.medium
                )
            }
            when (scanState.state) {
                ScanState.CONFIG_PREPARE -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(state = rememberScrollState())
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(R.string.format))
                            if (showDeleteFormatOperation) {
                                TextButton(onClick = {
                                    showDeleteFormatOperation = false
                                }) {
                                    Text(
                                        text = stringResource(R.string.complete)
                                    )
                                }
                            } else {
                                IconButton(onClick = { showDeleteFormatOperation = true }) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_delete_24),
                                        contentDescription = stringResource(R.string.delete),
                                    )
                                }
                            }
                        }
                        FlexBox(
                            config = {
                                wrap(FlexWrap.Wrap)
                                gap(space)
                            }
                        ) {
                            list.forEachIndexed { index, item ->
                                Box(modifier = Modifier.clickable {
                                    if (!showDeleteFormatOperation) return@clickable
                                    list.removeAt(index)
                                }) {
                                    Text(
                                        text = item,
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .then(textPadding)
                                    )
                                    if (showDeleteFormatOperation) {
                                        Icon(
                                            painter = painterResource(R.drawable.baseline_delete_24),
                                            contentDescription = stringResource(R.string.delete),
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.align(Alignment.CenterEnd)
                                        )
                                    }
                                }
                            }
                            FilledIconButton(onClick = { showEditFormatDialog = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_add_24),
                                    contentDescription = stringResource(R.string.add)
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = MaterialTheme.ext.spacing.xs))
                        Text(text = stringResource(R.string.scan_filter))
                        var durationError by remember { mutableStateOf(false) }
                        var fileSizeError by remember { mutableStateOf(false) }
                        Column {
                            OutlinedTextField(
                                value = durationValue,
                                onValueChange = {
                                    if (durationValue.all { it.isDigit() } && durationValue.toLong() > 0) {
                                        durationValue = it
                                    } else {
                                        durationError = true
                                    }
                                },
                                supportingText = {
                                    if (durationError) {
                                        Text(
                                            text = stringResource(R.string.file_input_error),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                label = {
                                    Text(text = stringResource(R.string.duration))
                                },
                                trailingIcon = {
                                    Text(text = " S ")
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = fileSizeValue,
                                onValueChange = {
                                    if (fileSizeValue.all { it.isDigit() } && fileSizeValue.toLong() > 0) {
                                        fileSizeValue = it
                                    } else {
                                        fileSizeError = true
                                    }
                                },
                                label = {
                                    Text(text = stringResource(R.string.file_size))
                                },
                                supportingText = {
                                    if (fileSizeError) {
                                        Text(
                                            text = stringResource(R.string.file_input_error),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                trailingIcon = {
                                    Text(text = " KB ")
                                },

                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        HorizontalDivider()
                        ListItem(
                            modifier = Modifier
                                .clickable { nav3ViewModel.navigate(Route.FolderFilter) },
                            headlineContent = {
                                Text(text = stringResource(R.string.folder_filter))
                            },
                            trailingContent = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_keyboard_arrow_right_24),
                                    contentDescription = null
                                )
                            }
                        )
                        scanConfig.excludeFolders.forEach { folder ->
                            Text(
                                text = folder,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = MaterialTheme.ext.spacing.xs)
                            )
                        }
                    }
                }

                ScanState.START,
                ScanState.SCANNING,
                ScanState.DURATION,
                ScanState.FILTER,
                ScanState.RESULT -> {
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

                ScanState.CANCELLED -> {
                    ColumnCenterContainer {
                        Text(
                            text = stringResource(R.string.cancel_scan),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                ScanState.ERROR -> {
                    ColumnCenterContainer {
                        Text(
                            text = scanState.error.toString(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                ScanState.END -> {
                    LazyScanResultList(list=scanState.result.map { it.path })
//                    val paths = scanState.result.map { it.path }
////                        .map(Path::of)
//                        .map{ Paths.get(it) }
//                        .groupBy { it.parent }
//                        .map { (dir, files) ->
//                            AudioGroup(
//                                title = dir.fileName.toString(),
//                                parentPath = dir.parent.toString(),
//                                files = files.map { it.fileName.toString() }
//                            )
//                        }
//                    var expandedIndex by remember { mutableStateOf<Int?>(null) }
//                    LazyColumn(modifier = Modifier.fillMaxSize()) {
//                        itemsIndexed(paths) { index, group ->
//                            val expanded = expandedIndex == index
//                            ListItem(
//                                modifier = Modifier.clickable {
//                                    expandedIndex = if (expanded) null else index
//                                },
//                                trailingContent = {
//                                    Icon(
//                                        painter = painterResource(
//                                            if (expanded) {
//                                                R.drawable.baseline_keyboard_arrow_down_24
//                                            } else {
//                                                R.drawable.baseline_keyboard_arrow_right_24
//                                            }
//                                        ), contentDescription = null
//                                    )
//                                },
//                                headlineContent = {
//                                    Text(text = group.title)
//                                },
//                                supportingContent = {
//                                    Text(text = group.parentPath)
//                                },
//                            )
//                            AnimatedVisibility(visible = expanded) {
//                                Column(modifier = Modifier.padding(start = MaterialTheme.ext.spacing.large)) {
//                                    group.files.forEach { file ->
//                                        ListItem(
//                                            headlineContent = {
//                                                Text(text = file)
//                                            }
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
                }

            }
        }
    }
    if (showEditFormatDialog) {
        var value by remember { mutableStateOf("") }
        var error by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showEditFormatDialog = false },
            title = { Text(text = stringResource(R.string.format_add)) },
            text = {
                TextField(
                    isError = error,
                    supportingText = {
                        if (error) {
                            Text(
                                text = stringResource(R.string.error_name_tip),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    value = value,
                    onValueChange = { value = it }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (list.contains(value)) {
                        error = true
                        return@TextButton
                    } else {
                        list.add(value)
                    }
                    showEditFormatDialog = false
                }) { Text(text = stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditFormatDialog = false
                }) { Text(text = stringResource(R.string.cancel)) }
            }
        )
    }
}

//@Composable
//fun ScanLoading(
//    modifier: Modifier = Modifier,
//    cornerColor: Color = Color.White,
//    lineColor: Color = Color.White,
//    cornerLength: Dp = 28.dp,
//    strokeWidth: Dp = 3.dp
//) {
//    val infiniteTransition = rememberInfiniteTransition(label = "scanLoading")
//    val progress by infiniteTransition.animateFloat(
//        initialValue = 0f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(
//                durationMillis = 1800,
//                easing = LinearEasing
//            ), repeatMode = RepeatMode.Restart
//        ),
//        label = "scanProgress"
//    )
//    Canvas(modifier = modifier) {
//        val width = size.width
//        val height = size.height
//        val corner = cornerLength.toPx()
//        val stroke = strokeWidth.toPx()
//
//        drawLine(
//            color = cornerColor,
//            start = Offset(0f, corner),
//            end = Offset(0f, 0f),
//            strokeWidth = stroke
//        )
//        drawLine(
//            color = cornerColor,
//            start = Offset(0f, 0f),
//            end = Offset(corner, 0f),
//            strokeWidth = stroke
//        )
//
//        drawLine(
//            color = cornerColor,
//            start = Offset(width - corner, 0f),
//            end = Offset(width, 0f),
//            strokeWidth = stroke
//        )
//        drawLine(
//            color = cornerColor,
//            start = Offset(width, 0f),
//            end = Offset(width, corner),
//            strokeWidth = stroke
//        )
//
//        drawLine(
//            color = cornerColor,
//            start = Offset(0f, height - corner),
//            end = Offset(0f, height),
//            strokeWidth = stroke
//        )
//        drawLine(
//            color = cornerColor,
//            start = Offset(0f, height),
//            end = Offset(corner, height),
//            strokeWidth = stroke
//        )
//
//        drawLine(
//            color = cornerColor,
//            start = Offset(width - corner, height),
//            end = Offset(width, height),
//            strokeWidth = stroke
//        )
//        drawLine(
//            color = cornerColor,
//            start = Offset(width, height - corner),
//            end = Offset(width, height),
//            strokeWidth = stroke
//        )
//        val scanY = height * progress
////        drawLine(
////            color = lineColor,
////            start = Offset(0f, scanY),
////            end = Offset(width, scanY),
////            strokeWidth = stroke
////        )
//        drawRect(
//            brush = Brush.verticalGradient(
//                colors = listOf(
//                    Color.Transparent,
//                    lineColor.copy(alpha = 0.15f),
//                    lineColor,
//                    lineColor.copy(alpha = 0.15f),
//                    Color.Transparent,
//                ),
//                startY = scanY - 20.dp.toPx(),
//                endY = scanY + 20.dp.toPx()
//            ),
//            topLeft = Offset(0f, scanY - 20.dp.toPx()),
//            size = Size(width, 40.dp.toPx())
//        )
//    }
//
//}