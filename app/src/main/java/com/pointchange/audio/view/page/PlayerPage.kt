package com.pointchange.audio.view.page

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.PlayerViewModel
import com.pointchange.audio.model.SettingViewModel
import com.pointchange.audio.model_data.PlayMode.LOOP
import com.pointchange.audio.model_data.PlayMode.RANDOM
import com.pointchange.audio.model_data.PlayMode.REPEAT
import com.pointchange.audio.util.getUriFromPath
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import com.pointchange.audio.view.component.Cover
import com.pointchange.audio.view.component.Lrc
import com.pointchange.audio.view.component.PlayPauseBtn
import com.pointchange.audio.view.component.ProgressAndText
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerPage(
    audioListViewModel: AudioListViewModel = viewModel(),
    playerViewModel: PlayerViewModel = viewModel(),
    settingViewModel: SettingViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val lrcContent by audioListViewModel.lrcContent.collectAsStateWithLifecycle()

    val backgroundColor by audioListViewModel.colorFromCover.collectAsStateWithLifecycle()
    val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
    val navActions = LocalNav.current
    val playingAudioMetadata = playingInfo.audioMetadata
    val uri = playingAudioMetadata?.uri ?: ""
    val title = playingAudioMetadata?.title ?: ""
    val artist = playingAudioMetadata?.artist ?: ""
    val album = playingAudioMetadata?.album ?: ""
    var favor by remember(playingAudioMetadata?.isFavorite) {
        mutableStateOf(
            playingAudioMetadata?.isFavorite ?: false
        )
    }
    val textAndIconColor = Color.White

    val shareText = stringResource(R.string.share_to)

    var showMultipPlayBackBottomSheet by remember { mutableStateOf(false) }
    val multipPlayBacksheetState = rememberModalBottomSheetState()
    val rate by playerViewModel.multiplePlayBack.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()


    val tabs = listOf(stringResource(R.string.tab_0), stringResource(R.string.tab_1))
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val themeConfig by settingViewModel.themeConfig.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(backgroundColor)
            ),
    ) {
        Scaffold(
            containerColor = audioListViewModel.transparencyColor,
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = audioListViewModel.transparencyColor),
                    navigationIcon = {
                        IconButton(
                            onClick = { navActions?.removeLast() }) {
                            Icon(
                                painter = painterResource(R.drawable.round_navigate_before_24),
                                contentDescription = stringResource(R.string.back),
                                tint = textAndIconColor
                            )
                        }
                    },
                    title = {
                        PrimaryTabRow(
                            containerColor = Color.Transparent,
                            selectedTabIndex = pagerState.currentPage,
                            divider = {},
                            indicator = {
                                Box(
                                    modifier = Modifier
                                        .tabIndicatorOffset(pagerState.currentPage)
                                        .background(Color.White.copy(alpha = 0.12F))
                                        .height(1.dp)
                                ) {}
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selectedContentColor = textAndIconColor,
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(
                                                index
                                            )
                                        }
                                    }
                                ) {
                                    Text(text = title, color = textAndIconColor)
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { navActions?.add(NavID.AudioEffect) }) {
                            Icon(
                                painter = painterResource(R.drawable.round_tune_24),
                                contentDescription = stringResource(R.string.tone_control),
                                tint = textAndIconColor
                            )
                        }
                    })
            },
            bottomBar = {
                Surface(
                    color = audioListViewModel.transparencyColor,
                    modifier = Modifier
                        .wrapContentHeight()
                        .windowInsetsPadding(
                            WindowInsets.navigationBars
                        )
                ) {
                    Column() {
                        ProgressAndText(
                            currentTime = playerViewModel.current,
                            duration = playerViewModel.duration,
                            onValueChangeFinished = {
                                playerViewModel.seekTo(it)
                            },
                            textColors = textAndIconColor,
                            listItemColors = ListItemDefaults.colors(containerColor = audioListViewModel.transparencyColor)
                        )

                        ListItem(
                            leadingContent = {
                                IconButton(onClick = { audioListViewModel.setPlayingInfoMode() }) {
                                    Icon(
                                        painter = painterResource(
                                            id = when (playingInfo.mode) {
                                                LOOP -> R.drawable.round_repeat_24
                                                REPEAT -> R.drawable.round_repeat_one_24
                                                RANDOM -> R.drawable.round_shuffle_24
                                            }
                                        ),
                                        contentDescription = playingInfo.mode.getString(context),
                                        tint = textAndIconColor
                                    )
                                }
                            },
                            headlineContent = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(
                                        onClick = { audioListViewModel.previous() },
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.round_skip_previous_24),
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp),
                                            tint = textAndIconColor
                                        )
                                    }
                                    PlayPauseBtn(
                                        isPlaying = isPlaying,
                                        onClick = {
                                            if (audioListViewModel.playList.isEmpty()) return@PlayPauseBtn
                                            if (isPlaying) playerViewModel.pause() else playerViewModel.play()
                                        },
                                        tint = textAndIconColor,
                                        modifier = Modifier.size(80.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            audioListViewModel.next()
                                        },
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.round_skip_next_24),
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp),
                                            tint = textAndIconColor
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    audioListViewModel.updateFavoriteState()
                                    favor = !favor
                                }) {
                                    Icon(
                                        painter = painterResource(if (favor) R.drawable.round_favorite_24 else R.drawable.round_favorite_border_24),
                                        contentDescription = null,
                                        tint = textAndIconColor
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = audioListViewModel.transparencyColor)
                        )
                        //189.0.dp

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        color = Color.White.copy(alpha = 0.12F),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {

                                TextButton(onClick = {
                                    //onChangeShowLrc()
                                    navActions?.add(NavID.ScanLrc)
                                }) {
                                    Text(
                                        text = stringResource(R.string.lrc),
                                        color = textAndIconColor
                                    )
                                }
                                PlayerOtherOperation.entries.forEach {
                                    IconButton(onClick = {
                                        when (it) {
                                            PlayerOtherOperation.SHARE -> {
                                                val file = File(uri)
                                                val uri =
                                                    getUriFromPath(context, file)
                                                        ?: return@IconButton
                                                Intent(Intent.ACTION_SEND).apply {
                                                    type = "*/*"
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    clipData =
                                                        ClipData.newRawUri(
                                                            file.nameWithoutExtension,
                                                            uri
                                                        )
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    context.startActivity(
                                                        Intent.createChooser(
                                                            this,
                                                            shareText
                                                        )
                                                    )
                                                }
                                            }

                                            PlayerOtherOperation.INFO -> {
                                                audioListViewModel.parseAudioInfo(uri)
                                                navActions?.add(NavID.Information)
                                            }

                                            PlayerOtherOperation.MULTIPLE_PLAYBACK -> {
                                                showMultipPlayBackBottomSheet = true
                                            }
                                        }
                                    }) {
                                        Icon(
                                            painter = painterResource(it.icon),
                                            contentDescription = stringResource(it.text),
                                            tint = textAndIconColor
                                        )
                                    }

                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                HorizontalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> {
                            Column() {

                                ListItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ListItemDefaults.colors(containerColor = audioListViewModel.transparencyColor),
                                    overlineContent = {
                                        Cover(
                                            context = context,
                                            audioListViewModel = audioListViewModel,
                                            themeConfig = themeConfig,
                                            coverState = CoverState.PLAYER
                                        ) {
                                            audioListViewModel.setColorFromCover(it)
                                        }
                                    },
                                    headlineContent = {
                                        Text(
                                            text = title,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .padding(top = 16.dp)
                                                .basicMarquee(),
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = textAndIconColor
                                        )
                                    },
                                    supportingContent = {
                                        Column {
                                            if (playingAudioMetadata?.artist != null) {
                                                Text(
                                                    text = artist,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = textAndIconColor
                                                )
                                            }
                                            if (playingAudioMetadata?.album != null) {
                                                Text(
                                                    text = album,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = textAndIconColor

                                                )
                                            }
                                        }

                                    }
                                )
                            }
                        }

                        1 -> {
                            if (lrcContent.isNotEmpty()) {
                                Lrc(
                                    lrcContent = lrcContent,
                                    current = playerViewModel.current,
                                    onSeekTo = {
                                        playerViewModel.seekTo(it.toFloat())
                                    }
                                )
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    TextButton(onClick = {
                                        navActions?.add(NavID.ScanLrc)
                                    }) {
                                        Text(stringResource(R.string.lrc_input))
                                    }
                                }
                            }
                        }
                    }

                }

            }


            if (showMultipPlayBackBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showMultipPlayBackBottomSheet = false
                    },
                    sheetState = multipPlayBacksheetState
                ) {
                    MultiplePlayBack.entries.forEach {
                        ListItem(
                            headlineContent = { Text(text = it.text + "x") },
                            trailingContent = {
                                RadioButton(
                                    selected = (it.text.toFloat() == rate),
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable(onClick = {
                                playerViewModel.setMultiplePlayBack(
                                    it.text.toFloat()
                                )
                            })
                        )
                    }
                }
            }
        }
    }
}

