package com.pointchange.sampleaudio.ui.component

import android.net.Uri
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.Route
import com.pointchange.sampleaudio.data.SongInfo
import com.pointchange.sampleaudio.utils.handleString
import com.pointchange.sampleaudio.utils.toFormatTime
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel

@Composable
fun LazyPagerList(
    modifier: Modifier = Modifier,
    list: LazyPagingItems<SongInfo>,
    isShowCover: Boolean = false,
    isFirstLoadingData: Boolean = false,
    onClickItem: (id: Long, index: Int) -> Unit = { id, index -> },
) {
    val nav3ViewModel = viewModel<Nav3ViewModel>()
    Box(modifier = modifier) {
        if (list.itemCount > 0) {

            LazyColumnList(
                list = list.itemSnapshotList.items,
                isShowCover = isShowCover,
                onDetail = { path -> nav3ViewModel.navigate(Route.Info(path)) },
                onClickItem = { id, index -> onClickItem(id, index) }
            )


        } else {
            when (list.loadState.refresh) {
                is LoadState.Error -> {
                    ColumnCenterContainer {
                        Icon(
                            painter = painterResource(R.drawable.twotone_error_24),
                            contentDescription = stringResource(R.string.error),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.empty_data),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                LoadState.Loading -> {
                    if (!isFirstLoadingData) {
                        ColumnCenterContainer {
                            CircularProgressIndicator()
                        }
                    }
                }

                is LoadState.NotLoading -> {
                    ColumnCenterContainer {
                        Text(
                            text = stringResource(R.string.empty_data),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }

    }
}

