package com.pointchange.sampleaudio.ui.page

import android.content.ClipData
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.indicator.PositionAndDurationText
import androidx.media3.ui.compose.material3.indicator.ProgressSlider
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.Route
import com.pointchange.sampleaudio.player.lyrics.LyricsLine
import com.pointchange.sampleaudio.player.lyrics.LyricsUiState
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.ui.component.ColumnCenterContainer
import com.pointchange.sampleaudio.ui.component.DefineSlider
import com.pointchange.sampleaudio.ui.component.LyricsItemTextDialog
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.utils.handleString
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsPage() {
    val viewModel = viewModel<PlayerStateViewModel>()
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val currentState by viewModel.currentState.collectAsStateWithLifecycle()
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val player by viewModel.controllerFuture.collectAsStateWithLifecycle()

    LaunchedEffect(currentState.id) {
        viewModel.loadLyrics(currentState.id)
    }
    val lyricsUiState by viewModel.lyricsUiState.collectAsStateWithLifecycle()
    val positionMs by viewModel.currentTimeMs.collectAsStateWithLifecycle()
    val labelColor = if (settingsState.currentPlayCoverColor.isEmpty()) {
        Color.Unspecified
    } else {
        if (settingsState.currentPlayCoverColor[2].luminance() > 0.55f) {
            Color.Black.copy(alpha = 0.87f)
        } else {
            Color.White.copy(alpha = 0.95f)
        }
    }
    val brushList = if (settingsState.currentPlayCoverColor.isEmpty()) {
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
        )
    } else {
        listOf(
            settingsState.currentPlayCoverColor[0].darkHsl(0.15f),
            settingsState.currentPlayCoverColor[1].darkHsl(0.15f),
        )
    }
    val (title, subTitle) = handleString(
        path = currentState.path ?: "",
        title = currentState.title,
        artist = currentState.artist,
        album = currentState.album
    )

    var expanded by remember { mutableStateOf(false) }
    val menuItems = listOf(
        Triple(R.drawable.baseline_find_in_page_24, R.string.scan_all_lyrics_file) {
            nav3ViewModel.navigate(Route.ScanLrc)
            expanded = false
        },
        Triple(R.drawable.outline_info_24, R.string.lyrics_info) {
            nav3ViewModel.navigate(Route.LyricsInformation)
            expanded = false
        },
    )
    Box(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                brushList
            )
        )
    ) {

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        BackButton(tint = labelColor) { nav3ViewModel.pop() }
                    },
                    title = {
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(),
                                color = labelColor
                            )
                            Text(
                                text = subTitle,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.basicMarquee(),
                                color = labelColor.copy(alpha = 0.60f)
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_more_vert_24),
                                    contentDescription = stringResource(R.string.more),
                                    tint = labelColor
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                menuItems.forEach { (icon, text, onClick) ->
                                    DropdownMenuItem(
                                        text = { Text(text = stringResource(text)) },
                                        leadingIcon = {
                                            Icon(
                                                painterResource(icon),
                                                contentDescription = null,
                                            )
                                        },
                                        onClick = onClick
                                    )
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier.padding(
                        start = MaterialTheme.ext.spacing.medium,
                        top = MaterialTheme.ext.spacing.medium,
                        end = MaterialTheme.ext.spacing.medium,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    )
                ) {
//                    ProgressSlider(player = player)
                    DefineSlider(
                        activeTrackColor = labelColor,
                        inactiveTrackColor = if (settingsState.currentPlayCoverColor.isEmpty()) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            if (settingsState.currentPlayCoverColor[2].luminance() > 0.55f) {
                                Color.DarkGray.copy(alpha = 0.87f)
                            } else {
                                Color.Gray.copy(alpha = 0.95f)
                            }
                        }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PlayPauseButton(
                                player = player,
                                colors = IconButtonDefaults.iconButtonColors(contentColor = labelColor)
                            )
                            IconButton(onClick = { viewModel.libraryQueueAsFn.previous(player) }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_skip_previous_24),
                                    contentDescription = stringResource(R.string.previous),
                                    tint = labelColor
                                )
                            }
                            IconButton(onClick = { viewModel.libraryQueueAsFn.next(player) }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_skip_next_24),
                                    contentDescription = stringResource(R.string.next),
                                    tint = labelColor
                                )
                            }
                        }
                        PositionAndDurationText(player = player, color = labelColor)
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (lyricsUiState) {
                    LyricsUiState.Empty -> {
                        ColumnCenterContainer {
                            Text(text = stringResource(R.string.empty_data))
                        }
                    }

                    is LyricsUiState.Error -> {
                        ColumnCenterContainer {
                            Text(text = (lyricsUiState as LyricsUiState.Error).message)
                        }
                    }

                    LyricsUiState.Loading -> {
                        ColumnCenterContainer {
                            CircularProgressIndicator()
                        }
                    }

                    is LyricsUiState.Success -> {
                        val listState = rememberLazyListState()
                        val lines = (lyricsUiState as LyricsUiState.Success).document.line
                        val currentIndex by remember(lines, positionMs) {
                            derivedStateOf {
                                findCurrentLyricIndex(lines = lines, positionMs = positionMs)
                            }
                        }

                        var autoScroll: Boolean by remember { mutableStateOf(true) }
                        LaunchedEffect(currentIndex, autoScroll) {
                            if (!autoScroll || currentIndex < 0) return@LaunchedEffect

                            listState.animateScrollToItem(
                                index = currentIndex,
                            )

                        }

                        LaunchedEffect(listState) {
                            listState.interactionSource.interactions.collect { scrolling ->
                                if (scrolling is DragInteraction) {
                                    autoScroll = false
                                }
                            }
                        }
                        LaunchedEffect(currentState.id) {
                            autoScroll = true
                            listState.scrollToItem(0)
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    vertical = 280.dp,
                                    horizontal = MaterialTheme.ext.spacing.large
                                )
                            ) {
                                items(items = lines, key = { it.index }) { line ->
                                    LyricsLineItem(
                                        line = line,
                                        active = line.index == currentIndex,
                                        onClick = {
                                            viewModel.seekTo(line.startTimeMs)
                                            autoScroll = true
                                        }
                                    )
                                }
                            }
                            AnimatedVisibility(
                                visible = !autoScroll,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        MaterialTheme.ext.spacing.large
                                    )
                            ) {
                                SmallFloatingActionButton(
                                    onClick = {
                                        autoScroll = true
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_location_on_24),
                                        contentDescription = stringResource(R.string.current_lyric)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsLineItem(
    line: LyricsLine,
    active: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (active) 1f else 0.94f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "lyricScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (active) 1f else 0.42f,
        animationSpec = tween(300),
        label = "lyricAlpha"
    )
    val color by animateColorAsState(
        targetValue = if (active) Color.White else Color.White.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "lyricColor"
    )
    Text(
        text = line.text,
        color = color,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
            lineHeight = 34.sp,
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .clip(MaterialTheme.shapes.medium)
            .padding(
                horizontal = MaterialTheme.ext.spacing.xs,
                vertical = MaterialTheme.ext.spacing.xs
            )
    )
}

fun findCurrentLyricIndex(lines: List<LyricsLine>, positionMs: Long): Int {
    if (lines.isEmpty()) return -1;
    var low = 0
    var high = lines.lastIndex
    var result = -1
    while (low <= high) {
        val mid = (low + high) ushr 1
        if (lines[mid].startTimeMs <= positionMs) {
            result = mid
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return result
}