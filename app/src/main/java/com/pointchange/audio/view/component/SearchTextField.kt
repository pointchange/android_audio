package com.pointchange.audio.view.component

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pointchange.audio.R
import com.pointchange.audio.model.AudioListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    searchBarState: SearchBarState,
    drawerHandle:()->Unit={},
    audioListViewModel: AudioListViewModel = viewModel(),
) {
    val textFieldState = rememberTextFieldState()
    val scope = rememberCoroutineScope()
    SearchBarDefaults.InputField(
        modifier = Modifier,
        searchBarState = searchBarState,
        textFieldState = textFieldState,
        onSearch = {
            audioListViewModel.setKeyword(it)
        },
        placeholder = { Text(stringResource(R.string.search_song_album_artist)) },
        leadingIcon = {

            if (searchBarState.currentValue == SearchBarValue.Expanded) {
                IconButton(
                    onClick = { scope.launch { searchBarState.animateToCollapsed() } }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_navigate_before_24),
                        contentDescription = stringResource(R.string.drawer_sheet),
                    )
                }

            } else {
                IconButton( onClick = drawerHandle) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_density_medium_24),
                        contentDescription = stringResource(R.string.more)
                    )
                }
            }
        },
        trailingIcon = {
            if (searchBarState.currentValue == SearchBarValue.Expanded) {
                IconButton(
                    onClick = { textFieldState.clearText() }
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.round_close_24),
                        contentDescription = stringResource(R.string.clear_input_value)
                    )

                }
            } else {
                IconButton(
                    onClick = { scope.launch { searchBarState.animateToExpanded()} }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_search_24),
                        contentDescription = stringResource(R.string.search_music)
                    )
                }

            }
        },
    )
}