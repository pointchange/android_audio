package com.pointchange.audio.view.page

import android.util.Log
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarState
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.PlayList
import com.pointchange.audio.view.component.MusicList
import com.pointchange.audio.view.component.SearchTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    searchBarState: SearchBarState,
    audioListViewModel: AudioListViewModel = viewModel()
) {
    val searchAudio = audioListViewModel.searchAudio.collectAsLazyPagingItems()
    ExpandedFullScreenSearchBar(
        state = searchBarState,
        inputField = { SearchTextField(searchBarState = searchBarState) }) {
        MusicList(list = searchAudio, playList = PlayList.SEARCH,viewModel = audioListViewModel)
    }
}