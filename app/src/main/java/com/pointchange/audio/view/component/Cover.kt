package com.pointchange.audio.view.component

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.ThemeConfig
import com.pointchange.audio.view.page.CoverState
import com.pointchange.audio.view.state.rememberIsDark

@Composable
fun Cover(
    modifier: Modifier = Modifier,
    context: Context,
    audioListViewModel: AudioListViewModel,
    themeConfig: ThemeConfig,
    coverState: CoverState = CoverState.BAR,
    setColorFromCover: (bitmap: Bitmap?) -> Unit,
) {
    var loadingCoverError by remember { mutableStateOf(false) }
    val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
    val isDark = rememberIsDark(themeConfig = themeConfig)
    Box(modifier = modifier) {
        if (playingInfo.audioMetadata != null && playingInfo.audioMetadata?.coverPath != "") {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(playingInfo.audioMetadata?.coverPath)
                    .allowHardware(false)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(10.dp))
                    .then(
                        if (coverState == CoverState.PLAYER) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier
                        }
                    ),
                onLoading = {
                    loadingCoverError = false
                },
                onSuccess = { state ->
                    val bitmap = state.result.drawable.toBitmap()
                    setColorFromCover(bitmap)
                },
                onError = {
                    loadingCoverError = true
                }
            )
        }

        if (loadingCoverError) {
            if (coverState == CoverState.PLAYER) {
                Image(
                    painter = painterResource(R.drawable.dark_cover),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(10.dp))
                        .fillMaxWidth(),
                )
            } else {
                Image(
                    painter = painterResource(if (isDark) R.drawable.dark_cover else R.drawable.light_cover),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(10.dp))
                        .fillMaxHeight(),
                )
            }

            ////                            mostColor = Color(0xFF666666),
            ////                            remainColor = Color.DarkGray,
//            Image(
//                painter = painterResource(
//                    if (isDark || coverState == CoverState.PLAYER) {
//                        R.drawable.dark_cover
//                    } else {
//                        R.drawable.light_cover
//                    }
//                ),
//                contentDescription = null,
//                contentScale = ContentScale.Fit,
//                modifier = Modifier
//                    .clip(shape = RoundedCornerShape(10.dp))
//                    .then(
//                        if (coverState == CoverState.PLAYER) {
//                            Modifier.fillMaxWidth()
//                        } else {
//                            Modifier
//                        }
//                    ),
//            )


//            if (isDark || coverState == CoverState.PLAYER) {
//                AudioCover(
//                    mostColor = Color(0xFF444444),
//                    remainColor = Color(0xFF222222)
//                )

//            } else {
//                if (coverState == CoverState.BAR) {
//                    AudioCover(sizeInt = 64.dp)
//                } else {
//                    AudioCover()
//                }
//            }
            setColorFromCover(null)
        }

    }
}