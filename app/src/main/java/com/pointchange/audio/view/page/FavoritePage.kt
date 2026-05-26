package com.pointchange.audio.view.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.R
import com.pointchange.audio.view.NavID
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.PlayList
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.view.component.MusicList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePage(
    audioListViewModel: AudioListViewModel = viewModel()
) {
    val navActions = LocalNav.current
    val audioList: LazyPagingItems<AudioMetadata> =
        audioListViewModel.audioList.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(navigationIcon = {
                IconButton(onClick = {
                    navActions?.removeLast()
                    audioListViewModel.setFavor(false)
                }) {
                    Icon(
                        painter = painterResource(R.drawable.round_navigate_before_24),
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, title = {
                Text(
                    text = stringResource(R.string.favorite),
                    style = MaterialTheme.typography.titleLarge
                )
            }, actions = {})
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            MusicList(list = audioList, playList = PlayList.FAVORITE, viewModel = audioListViewModel)
        }
    }
}