package com.pointchange.sampleaudio.ui.page

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.indicator.DurationText
import androidx.media3.ui.compose.material3.indicator.PositionText
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.Route
import com.pointchange.sampleaudio.ui.component.BackButton
import com.pointchange.sampleaudio.ui.component.Cover
import com.pointchange.sampleaudio.ui.component.DefineSlider
import com.pointchange.sampleaudio.ui.component.EqualizerScreen
import com.pointchange.sampleaudio.ui.component.VariableList
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.utils.handleString
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel

enum class PlayerSheetType {
    NONE,
    SPEED,
    PLAYLIST,
    AUDIO_EFFECT
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerPage() {
    val viewmodel = viewModel<PlayerStateViewModel>()
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    val controller by viewmodel.controllerFuture.collectAsStateWithLifecycle()
    val currentState by viewmodel.currentState.collectAsStateWithLifecycle()
    val settingsState by viewmodel.settingsState.collectAsStateWithLifecycle()

    var expanded by remember { mutableStateOf(false) }
    var currentSheet by remember { mutableStateOf(PlayerSheetType.NONE) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )
    val speeds = listOf(
        "0.5x" to 0.5f,
        "0.75x" to 0.75f,
        "1.0x" to 1.0f,
        "1.25x" to 1.25f,
        "1.5x" to 1.5f,
        "2.0x" to 2.0f,
    )
//    var showMask by remember { mutableStateOf(false) }
    val mask by animateColorAsState(
        targetValue = if (settingsState.showMask) Color.Black.copy(alpha = 0.34f) else Color.Transparent,
        label = "maskModifierColor"
    )
    val menuItems = listOf<Triple<Int, Int, () -> Unit>>(
        Triple(R.string.info, R.drawable.outline_info_24) {
            nav3ViewModel.navigate(Route.Info(currentState.path ?: ""))
            expanded = false
        },
        Triple(R.string.change_speed, R.drawable.baseline_1x_mobiledata_24) {
            currentSheet = PlayerSheetType.SPEED
            expanded = false
        },
        Triple(R.string.background_mask, R.drawable.baseline_imagesearch_roller_24) {
            viewmodel.setShowMask()
        }
    )
    val artworkUri = when {
        currentState.coverPath == null -> null
        else -> currentState.coverPath.toString()
    }
    val (title, subTitle) = handleString(
        path = currentState.path ?: "",
        title = currentState.title,
        artist = currentState.artist,
        album = currentState.album
    )

    val playListModes = listOf(
        R.string.repeat to R.drawable.baseline_repeat_24,
        R.string.shuffle to R.drawable.baseline_shuffle_24,
        R.string.repeat_one to R.drawable.baseline_repeat_one_24,
    )
    val selectedPlayListMode by remember(currentState.mode) {
        derivedStateOf {
            playListModes[currentState.mode.ordinal]
        }
    }
    val labelColor = if (settingsState.currentPlayCoverColor.isEmpty()) {
        Color.Unspecified
    } else {
        if (settingsState.currentPlayCoverColor[0].luminance() > 0.55f) {
            Color.Black.copy(alpha = 0.87f)
        } else {
            Color.White.copy(alpha = 0.95f)
        }
    }
    val favors = listOf(
        Triple(
            R.string.not_favor,
            R.drawable.baseline_favorite_border_24,
            labelColor
        ),
        Triple(
            R.string.favor,
            R.drawable.baseline_favorite_24,
            Color(0xFFE91E63).copy(alpha = 0.84f)
        ),
    )
    val isFavorite by remember(currentState.isFavorite) {
        derivedStateOf {
            if (currentState.isFavorite) {
                favors[1]
            } else {
                favors[0]
            }
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

    val actionButtons = listOf(
        Triple(R.string.current_list, R.drawable.round_playlist_play_24) {
            currentSheet = PlayerSheetType.PLAYLIST
        },
        Triple(R.string.audio_effect, R.drawable.baseline_bar_chart_24) {
            currentSheet = PlayerSheetType.AUDIO_EFFECT
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
                    },
                    actions = {
                        actionButtons.forEach { (text, icon, onClick) ->
                            IconButton(onClick = onClick) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = stringResource(text),
                                    tint = labelColor
                                )
                            }
                        }

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
                                menuItems.forEach { (text, icon, onClick) ->
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
                val infiniteTransition = rememberInfiniteTransition(label = "arrow")
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 10f,
                    targetValue = -30f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 4000,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "arrowOffset"
                )
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "alpha"
                )
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            var totalDrag: Pair<Float, Float> = 0f to 0f
                            detectDragGestures(
                                onDragEnd = {
                                    nav3ViewModel.navigate(Route.Lyrics)
                                    totalDrag = 0f to 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDrag = dragAmount.x to dragAmount.y
                                    if (totalDrag.first < -50 || totalDrag.second < -50) {
                                        totalDrag = 0f to 0f
                                    }
                                }
                            )
                        }
                        .fillMaxWidth()
                        .padding(
                            top = 16.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues()
                                .calculateBottomPadding()
                        )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.round_keyboard_arrow_up_24),
                        contentDescription = stringResource(R.string.up),
                        modifier = Modifier
                            .offset(y = offsetY.dp)
                            .alpha(alpha),
                        tint = labelColor
                    )
                    Text(
                        text = stringResource(R.string.lrc),
                        color = labelColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clickable { viewmodel.setShowMask() }
//                        .background(
//                            color = mask,
//                            shape = MaterialTheme.shapes.extraLarge
//                        )
                        .wrapContentSize()
                ) {
                    Cover(
                        artworkUri = artworkUri,
                        modifier = Modifier
                            .heightIn(min = 100.dp, max = 400.dp)
                            .clip(shape = MaterialTheme.shapes.extraLarge)
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = mask,
                                shape = MaterialTheme.shapes.extraLarge
                            )
                            .matchParentSize()
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        color = labelColor,

                        )
                    Text(
                        text = subTitle.split("-").getOrNull(0)?.trim() ?: "",
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .basicMarquee()
                            .padding(top = MaterialTheme.ext.spacing.xs),
                        color = labelColor.copy(alpha = 0.87f),
                    )
                    Text(
                        text = subTitle.split("-").getOrNull(1)?.trim() ?: "",
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .basicMarquee()
                            .padding(top = MaterialTheme.ext.spacing.xs),
                        color = labelColor.copy(alpha = 0.87f),
                    )
                }
                Column(modifier = Modifier.fillMaxWidth()) {
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
//                    ProgressSlider(player = controller)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PositionText(player = controller, color = labelColor)
                        DurationText(player = controller, color = labelColor)
                    }
                }

                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    leadingContent = {
                        IconButton(onClick = { viewmodel.libraryQueueAsFn.switchPlayerMode() }) {
                            Icon(
                                painter = painterResource(selectedPlayListMode.second),
                                contentDescription = stringResource(selectedPlayListMode.first),
                                tint = labelColor
                            )
                        }
                    },
                    headlineContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                modifier = Modifier
                                    .sizeIn(
                                        minWidth = 48.dp, minHeight = 48.dp,
                                        maxWidth = 64.dp, maxHeight = 64.dp
                                    ),
                                onClick = { viewmodel.libraryQueueAsFn.previous(controller) }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_skip_previous_24),
                                    contentDescription = stringResource(R.string.previous),
                                    tint = labelColor
                                )
                            }

                            PlayPauseButton(
                                player = controller,
                                modifier = Modifier
                                    .sizeIn(
                                        minWidth = 72.dp, minHeight = 72.dp,
                                        maxWidth = 120.dp, maxHeight = 120.dp
                                    ),
                                colors = IconButtonDefaults.iconButtonColors(contentColor = labelColor)
                            )
                            IconButton(
                                modifier = Modifier
                                    .sizeIn(
                                        minWidth = 48.dp, minHeight = 48.dp,
                                        maxWidth = 64.dp, maxHeight = 64.dp
                                    ),
                                onClick = { viewmodel.libraryQueueAsFn.next(controller) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_skip_next_24),
                                    contentDescription = stringResource(R.string.next),
                                    tint = labelColor
                                )
                            }
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { viewmodel.setFavor(currentState.id) }) {
                            Icon(
                                painter = painterResource(isFavorite.second),
                                contentDescription = stringResource(isFavorite.first),
                                tint = isFavorite.third
                            )
                        }
                    }
                )
            }
        }

        if (currentSheet != PlayerSheetType.NONE) {
            ModalBottomSheet(
                modifier = Modifier.fillMaxSize(),
                sheetState = sheetState,
                onDismissRequest = { currentSheet = PlayerSheetType.NONE }
            ) {
                when (currentSheet) {
                    PlayerSheetType.NONE -> {}
                    PlayerSheetType.SPEED -> {
                        speeds.forEach { (text, data) ->
                            Text(
                                text = text,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (currentState.speed == data) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    Color.Unspecified
                                },
                                modifier = Modifier
                                    .clickable(onClick = { viewmodel.setPlaySpeed(data) })
                                    .fillMaxWidth()
                                    .background(
                                        color = if (currentState.speed == data) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            Color.Unspecified
                                        },
                                        shape = MaterialTheme.shapes.extraLarge
                                    )
                                    .padding(16.dp)
                            )
                        }

                    }

                    PlayerSheetType.PLAYLIST -> {
                        VariableList(currentId = currentState.id, color = settingsState.currentPlayCoverColor)
                    }

                    PlayerSheetType.AUDIO_EFFECT -> {
                        EqualizerScreen(sheetState)
                    }
                }
            }

        }
    }
}

fun Color.darkHsl(amount: Float = 0.2f): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)
    hsl[2] = (hsl[2] - amount).coerceAtLeast(0f)
    return Color(ColorUtils.HSLToColor(hsl))
}