package com.pointchange.audio.view.page

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopSearchBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.pointchange.audio.view.LocalNav
import com.pointchange.audio.R
import com.pointchange.audio.view.NavID
import com.pointchange.audio.model.AudioListViewModel
import com.pointchange.audio.model.PlayList
import com.pointchange.audio.model.SettingViewModel
import com.pointchange.audio.model.ThemeMode
import com.pointchange.audio.model_data.SortAudio
import com.pointchange.audio.service.VlcManager
import com.pointchange.audio.view.component.Cover
import com.pointchange.audio.view.component.DraggableBarContainer
import com.pointchange.audio.view.component.MusicControlBar
import com.pointchange.audio.view.component.MusicList
import com.pointchange.audio.view.component.PermissionRequest
import com.pointchange.audio.view.component.SearchTextField
import com.pointchange.audio.view.component.allFilesPermission
import kotlinx.coroutines.launch


@SuppressLint("FrequentlyChangingValue")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    audioListViewModel: AudioListViewModel = viewModel(),
    settingViewModel: SettingViewModel = viewModel(),
) {
    val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
    val searchBarState = rememberSearchBarState()
    val context = LocalContext.current
    val audioPages = audioListViewModel.sortAudio.collectAsLazyPagingItems()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sortOrder by audioListViewModel.sortOrder.collectAsStateWithLifecycle()

    val navActions = LocalNav.current

    val themeConfig by settingViewModel.themeConfig.collectAsStateWithLifecycle()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                val themeModeList = ThemeMode.entries.toList()
                var index by remember { mutableIntStateOf(themeConfig.themeMode.ordinal) }
                Scaffold(
                    bottomBar = {
                        TextButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 64.dp),
                            onClick = {
                                val mode = themeModeList.getOrElse(++index) {
                                    index = 0
                                    themeModeList[index]
                                }
                                settingViewModel.setThemeMode(mode = mode)
                            }) {
                            Text(text = stringResource(themeConfig.themeMode.text))
                        }
                    }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        Text(
                            text = stringResource(R.string.sort),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        SortAudio.entries.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(text = item.getString(context)) },
                                icon = {
                                    if (sortOrder == item) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.round_check_24),
                                            contentDescription = null,
                                        )
                                    }
                                },
                                selected = false,
                                onClick = {
                                    val state = PlayList.valueOf(item.name)
                                    audioListViewModel.setSortOrder(item)
                                    audioListViewModel.setCurrentPlayListState(state = state)
                                    audioListViewModel.setPlayingInfo(state = state)
                                }
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.list),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        NavigationDrawerItem(
                            label = { Text(text = stringResource(R.string.favorite)) },
                            selected = false,
                            onClick = {
                                audioListViewModel.setFavor(true)
                                navActions?.add(NavID.Favorite)
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = stringResource(R.string.more),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        NavigationDrawer.entries.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(text = item.getString(context)) },
                                selected = false,
                                onClick = {
                                    when (item) {
                                        NavigationDrawer.SCAN -> {
                                            navActions?.add(NavID.Scan)
                                        }

                                        NavigationDrawer.SETTINGS -> {
                                            navActions?.add(NavID.Setting)

                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopSearchBar(
                    state = searchBarState,
                    scrollBehavior = scrollBehavior,
                    inputField = {
                        SearchTextField(searchBarState = searchBarState, drawerHandle = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        })
                    }
                )
                SearchPage(searchBarState = searchBarState)
            },
            bottomBar = {
                BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                    MusicControlBar(
                        audioListViewModel = audioListViewModel,
                        themeConfig = themeConfig
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                if (audioPages.itemCount == 0) {
                    if (VlcManager.tempList.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    allFilesPermission(context.applicationContext) {
                                        audioListViewModel.scanHandle(context.applicationContext)
                                    }
                                } else {
//                                            Log.d("str123", "sdk<30")
                                }
                            }) {
                                Text(stringResource(R.string.scan_local))
                            }
                        }
                    } else {
                        //                        VlcManager.tempList.forEach { Text(text="${it.uri}") }
                        MusicList(
                            list = null,
                            viewModel = audioListViewModel,
                            tempList = VlcManager.tempList
                        )
                        //                        MusicList(list = flow{ emit(PagingData.from(VlcManager.tempList)) }.collectAsLazyPagingItems(), viewModel = audioListViewModel)
                    }

                } else {
                    MusicList(list = audioPages, viewModel = audioListViewModel)
                }
            }
        }
    }



    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionRequest(
            permission = Manifest.permission.READ_MEDIA_AUDIO,
            permissionText = stringResource(id = R.string.read_media_audio),
        ) {
        }
    } else {
        PermissionRequest(
            permission = Manifest.permission.READ_EXTERNAL_STORAGE,
            permissionText = stringResource(id = R.string.read_media_audio),
        ) {
        }
    }
}


