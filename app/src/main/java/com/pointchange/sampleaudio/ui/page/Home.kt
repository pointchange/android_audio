package com.pointchange.sampleaudio.ui.page

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.pointchange.sampleaudio.R
import com.pointchange.sampleaudio.data.QueueListName
import com.pointchange.sampleaudio.data.Route
import com.pointchange.sampleaudio.data.Route.ManageList
import com.pointchange.sampleaudio.data.ThemeMode
import com.pointchange.sampleaudio.player.queue.LibraryQueue
import com.pointchange.sampleaudio.player.queue.QueueData
import com.pointchange.sampleaudio.remember.rememberUserPreferences
import com.pointchange.sampleaudio.ui.component.ItemBar
import com.pointchange.sampleaudio.ui.component.LazyColumnList
import com.pointchange.sampleaudio.ui.component.LazyPagerList
import com.pointchange.sampleaudio.ui.component.SearchBarIconButton
import com.pointchange.sampleaudio.ui.theme.ext
import com.pointchange.sampleaudio.utils.defineSubList
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(viewModel: PlayerStateViewModel = viewModel()) {
    val nav3ViewModel = viewModel<Nav3ViewModel>()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var searchBarExpanded by rememberSaveable { mutableStateOf(false) }

    val query by viewModel.query.collectAsStateWithLifecycle()
    val queryResults = viewModel.searchResultByQuery.collectAsLazyPagingItems()
    var showSettings by remember { mutableStateOf(false) }

    val count by viewModel.count.collectAsStateWithLifecycle()
    var mainListSwitchUIIndex by remember { mutableIntStateOf(0) }
    var mainListShowCover by remember { mutableStateOf(false) }
    val mainListSwitchUI = listOf<Triple<Int, Int, (i: Int) -> Unit>>(
        Triple(R.string.list, R.drawable.baseline_reorder_24) {
            mainListShowCover = false
            mainListSwitchUIIndex = it
        },
        Triple(R.string.list_cover, R.drawable.baseline_format_list_bulleted_24, {
            mainListShowCover = true
            mainListSwitchUIIndex = it
        })
    )
    val queue by viewModel.libraryQueueAsFn.queue.collectAsStateWithLifecycle()
    var queueSelected by remember { mutableStateOf(QueueListName.DEFAULT.label) }
    fun getQueueList(sourceList: List<QueueData>, name: String): List<Long> =
        sourceList.find { it.name == name }?.list ?: emptyList()

    val pagingItems =
        viewModel.getPaging(
            queueSelected,
            getQueueList(queue, queueSelected)
        )
            .collectAsLazyPagingItems()
    val queueNameList = queue.map { it.name }
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    var showDeleteAllDialong by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                if (queueNameList.isEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = stringResource(R.string.empty_list),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                } else {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    TextButton(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        onClick = {
                                            nav3ViewModel.navigate(Route.ManageList)
                                        }) { Text(text = stringResource(R.string.list_operation)) }
                                }
                            )
                        }
                    ) { paddingValues ->
                        LazyColumn(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxWidth()
                        ) {
                            items(
                                count = queueNameList.size,
                                key = { index -> queueNameList[index] }) { index ->
                                val name = queueNameList[index]
                                Row(
                                    modifier = Modifier.clickable(onClick = {
                                        queueSelected = name
                                    })
                                ) {
//                                if (queueSelected.first == name) {
                                    if (queueSelected == name) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 24.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(
                                                        topEndPercent = 50,
                                                        bottomEndPercent = 50
                                                    )
                                                )
                                                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                                                .basicMarquee()
                                        )

                                    } else {
                                        Text(
                                            text = name,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                                .basicMarquee()
                                        )
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                val searchBarLeadingIconList = listOf<Triple<Int, Int, () -> Unit>>(
                    Triple(R.string.list, R.drawable.baseline_menu_24) {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    Triple(R.string.back, R.drawable.baseline_arrow_back_24) {
                        viewModel.setQuery()
                        searchBarExpanded = false
                    },
                )

                val searchBarTrailingIconList = listOf<Triple<Int, Int, () -> Unit>>(
                    Triple(R.string.settings, R.drawable.round_settings_24) {
                        showSettings = true
                    },
                    Triple(R.string.clear, R.drawable.baseline_close_24) {
                        viewModel.setQuery()
                    },
                )

                SearchBar(
                    modifier = Modifier
                        .semantics { traversalIndex = 0f }
                        .padding(horizontal = if (searchBarExpanded) 0.dp else 16.dp),
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = query,
                            onQueryChange = { viewModel.setQuery(it) },
                            onSearch = {
                                searchBarExpanded = false
                            },
                            expanded = searchBarExpanded,
                            onExpandedChange = { searchBarExpanded = it },
                            placeholder = { Text(text = stringResource(R.string.search)) },
                            leadingIcon = {
                                val searchBarLeadingIconState =
                                    if (searchBarExpanded) searchBarLeadingIconList.last() else searchBarLeadingIconList.first()
                                SearchBarIconButton(searchBarLeadingIconState)
                            },
                            trailingIcon = {
                                if (searchBarExpanded) {
                                    SearchBarIconButton(
                                        searchBarTrailingIconList.last(),
                                        show = query.isNotEmpty()
                                    )
                                } else {
                                    SearchBarIconButton(searchBarTrailingIconList.first())
                                }
                            }
                        )
                    },
                    expanded = searchBarExpanded,
                    onExpandedChange = { searchBarExpanded = it },
                ) {
                    LazyPagerList(
                        modifier = Modifier.fillMaxSize(),
                        list = queryResults,
                        onClickItem = { id, index ->
                            viewModel.prepare(id, index)
                            //每搜一次就重置
                            val queueName = LibraryQueue.defaultCurrentUserQueue
//                            queue[queueName]?.let {
////                                queueSelected = LibraryQueue.defaultCurrentUserQueue to it.list
//                            }
                            queueSelected = LibraryQueue.defaultCurrentUserQueue
                            viewModel.libraryQueueAsFn.setQueueStateTitleAndList(
                                title = "搜索",
                                list = emptyList(),
                                index = index,
                                query = query
                            )
                        },
                    )
                }
            },
            bottomBar = {
                ItemBar()
            }
        ) { paddingValues ->

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (count > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.clickable(
                                onClick = {
                                    if (pagingItems.itemCount > 0) {
                                        pagingItems.itemSnapshotList.getOrNull(0)?.let {
                                            viewModel.prepare(it.id, 0)
                                            viewModel.libraryQueueAsFn.setQueueStateTitleAndList(
                                                title = queueSelected,
                                                list = getQueueList(queue, queueSelected)
                                            )
                                        }
                                    }
                                }
                            )
                        ) {
                            IconButton(
                                modifier = Modifier.padding(end = 8.dp),
                                onClick = {}
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_play_circle_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(text = stringResource(R.string.play_all) + " ${pagingItems.itemCount}")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = {
                                nav3ViewModel.navigate(
                                    Route.EditList(
                                        queueSelected,
                                        getQueueList(queue, queueSelected)
                                    )
                                )
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_edit_note_24),
                                    contentDescription = stringResource(R.string.edit_list),
                                )
                            }
                            mainListSwitchUI.forEachIndexed { index, (text, icon, onClick) ->
                                IconButton(onClick = { onClick(index) }) {
                                    Icon(
                                        painter = painterResource(icon),
                                        contentDescription = stringResource(text),
                                        tint = if (mainListSwitchUIIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            viewModel.setIsRefreshing(true)
                            viewModel.scanAllFile(context = context, autoComparable = true)
                        }
                    ) {
                        LazyPagerList(
                            list = pagingItems,
                            isShowCover = mainListShowCover,
                        ) { id, index ->
                            viewModel.prepare(id, index)
                            viewModel.libraryQueueAsFn.setQueueStateTitleAndList(
                                title = queueSelected,
                                list = defineSubList(
                                    getQueueList(queue, queueSelected),
                                    index
                                )
                            )
                        }
                    }

                } else {

                    if (viewModel.libraryQueueAsFn.firstLoadingData.isEmpty()) {
                        //扫描
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(60.dp, 80.dp)
                                .padding(horizontal = 16.dp),
                            onClick = { viewModel.scanAllFile(context = context) }
                        ) {
                            Text(
                                style = MaterialTheme.typography.displaySmall,
                                text = stringResource(
                                    R.string.scan
                                )
                            )
                        }
                    } else {
                        //
                        LazyColumnList(
                            list = viewModel.libraryQueueAsFn.firstLoadingData,
                            isShowCover = mainListShowCover,
                            onDetail = { path ->
                                nav3ViewModel.navigate(Route.Info(path))
                            },
                        ) { id, index ->
                            viewModel.prepare(id, index)
                            viewModel.libraryQueueAsFn.setQueueStateTitleAndList(
                                title = queueSelected,
                                list = defineSubList(
                                    getQueueList(queue, queueSelected),
                                    index
                                )
                            )
                        }
                    }

                }
            }
        }

        if (showSettings) {
            val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
            val textButtonState = when (settingsState.themeMode) {
                ThemeMode.FOLLOW_SYSTEM -> R.string.theme_follow_system
                ThemeMode.DARK -> R.string.theme_dark
                ThemeMode.LIGHT -> R.string.theme_light
            }

            ModalBottomSheet(onDismissRequest = { showSettings = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = MaterialTheme.ext.spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.ext.spacing.small)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.switchThemeMode() }
                    ) {
                        Text(text = stringResource(textButtonState))
                    }

                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            nav3ViewModel.navigate(Route.Timer)
                            showSettings = false
                        }
                    ) {
                        Text(text = stringResource(R.string.timer))
                    }

                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.resetScanState()
                            nav3ViewModel.navigate(Route.Scan)
                            showSettings = false
                        }
                    ) {
                        Text(text = stringResource(R.string.scan))
                    }

                    FilledTonalButton(
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showDeleteAllDialong = true
                        }
                    ) {
                        Text(text = stringResource(R.string.delete_all_database))
                    }
                }
            }
        }

    }
    if (showDeleteAllDialong) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialong = false },
            title = {
                Text(text = stringResource(R.string.delete_all_data_dialog_title))
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearDatabase()
                    showDeleteAllDialong = false
                }) { Text(text = stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteAllDialong = false
                }) { Text(text = stringResource(R.string.cancel)) }
            }
        )
    }
}

