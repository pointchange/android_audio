package com.pointchange.audio.view.page

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.util.copyToClipboard
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.view.NavID
import com.pointchange.audio.view.component.AudioCover
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.io.File
import kotlin.text.substringAfterLast

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformationPage(audioListViewModel: AudioListViewModel = viewModel()) {
    val navActions = LocalNav.current
    val audioMetadataDetail by
    audioListViewModel.audioMetadataDetail.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipToast = stringResource(R.string.clip_toast)
    var loadingCoverError by remember { mutableStateOf(false) }
    val (uri, artist, album, title, coverPath, channels, duration, bitrate, codec, sampleRate) = audioMetadataDetail
    val scrollState = rememberScrollState()
    val windowInfo = LocalWindowInfo.current
    val showTopBarText by remember {
        derivedStateOf {
            scrollState.value > windowInfo.containerSize.width
        }
    }
    var showSavePictureDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navActions?.removeLast() }) {
                        Icon(
                            painter = painterResource(R.drawable.round_navigate_before_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                title = {
                    if (showTopBarText || audioMetadataDetail.coverPath == null) {
                        Text(text = stringResource(R.string.audio_info))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        copyToClipboard(context, "$audioMetadataDetail", clipToast)
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.round_content_copy_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = if (audioMetadataDetail.coverPath != null) 0.dp else paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

                    .clickable(onClick = { showSavePictureDialog = true })
            ) {
                if (coverPath != null && !loadingCoverError) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(coverPath).allowHardware(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1F),
                        onSuccess = { },
                        onError = {
                            loadingCoverError = true
                        }
                    )
                }
            }
            Info.entries.forEach {
                val text = when (it) {
                    Info.TITLE -> if (title == "") stringResource(R.string.unknow_title) else title
                    Info.ARTIST -> if (artist == "") stringResource(R.string.unknow_artist) else artist
                    Info.ALBUM -> if (album == "") stringResource(R.string.unknow_album) else album
                    Info.CHANNELS -> channels.toString()
                    Info.BITRATE -> bitrate.toString()
                    Info.SAMPLE_RATE -> sampleRate.toString()
                    Info.DURATION -> duration.toString()
                    Info.FILE_TYPE -> codec
                    Info.FILE_SIZE -> String.format(
                        "%.2f MB",
                        File(uri).length() / 1024.0 / 1024.0
                    )

                    Info.FILE_PATH -> uri
                }
                ListItem(
                    headlineContent = {
                        Text(text = stringResource(it.text))
                    },
                    supportingContent = {
                        Text(
                            text = text
                        )
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = {},
                        onLongClick = {
                            copyToClipboard(context, text, clipToast)
                        }
                    )
                )
            }
        }
        if (showSavePictureDialog) {
            AlertDialog(
                title = {

                },
                text = {
                    Text(stringResource(R.string.Extract_cover))

                },
                onDismissRequest = {
                    showSavePictureDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (coverPath != null) {
                                audioListViewModel.saveInPublicPicture(
                                    context,
                                    uri = uri,
                                    coverPath = coverPath,
                                )
                            }
                            showSavePictureDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.save_cover))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSavePictureDialog = false }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

