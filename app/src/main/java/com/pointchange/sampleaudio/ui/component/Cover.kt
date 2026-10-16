package com.pointchange.sampleaudio.ui.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.ThemeMode
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Cover(
    artworkUri: String?,
    modifier: Modifier = Modifier,
) {

    val settingsState by viewModel<PlayerStateViewModel>().settingsState.collectAsStateWithLifecycle()

    val isDark = when (settingsState.themeMode) {
        ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val defaultCover = if (isDark) {
        R.drawable.dark_cover
    } else {
        R.drawable.light_cover
    }

    if (artworkUri == null) {
        Image(
            painter = painterResource(defaultCover),
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp, max = 400.dp),
            contentScale = ContentScale.Crop,
        )
    } else {
        AsyncImage(
            model = artworkUri,
            contentDescription = null,
            modifier = modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(defaultCover),
            error = painterResource(R.drawable.twotone_error_24)
        )

//        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
//        LaunchedEffect(artworkUri) {
//            bitmap = withContext(Dispatchers.IO) {
//                BitmapFactory.decodeFile(artworkUri)
//            }
//        }
//        bitmap?.let {
//            Image(
//                bitmap = it.asImageBitmap(), contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = modifier
//                    .fillMaxWidth()
//            )
//        }
    }
}