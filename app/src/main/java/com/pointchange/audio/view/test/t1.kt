package com.pointchange.audio.view.test

/**
 * 动画 home + player + bar + list
 *
 */

/**
 * box
 *
 * import androidx.compose.foundation.gestures.AnchoredDraggableState
 * import androidx.compose.foundation.gestures.Orientation
 * import androidx.compose.foundation.gestures.anchoredDraggable
 * import androidx.compose.foundation.layout.Box
 * import androidx.compose.foundation.layout.fillMaxSize
 * import androidx.compose.foundation.layout.offset
 * import androidx.compose.runtime.Composable
 * import androidx.compose.ui.Modifier
 * import androidx.compose.ui.unit.IntOffset
 * import com.pointchange.audio.view.page.PlayerDragValue
 * import kotlin.math.roundToInt
 *
 * @Composable
 * fun DraggableBarContainer(
 *     state: AnchoredDraggableState<PlayerDragValue>,
 *     content: @Composable () -> Unit
 * ) {
 *     Box(
 *         modifier = Modifier
 *             .fillMaxSize()
 *             .offset { IntOffset(0, state.requireOffset().roundToInt()) }
 *             .anchoredDraggable(
 *                 state = state,
 *                 orientation = Orientation.Vertical,
 *                 overscrollEffect = null
 *             )
 *     ) {
 *         content()
 *     }
 * }
 *
 */

/**
 * home
 *
 * package com.pointchange.audio.view.page
 *
 * import android.Manifest
 * import android.annotation.SuppressLint
 * import android.os.Build
 * import android.util.Log
 * import androidx.compose.foundation.gestures.AnchoredDraggableState
 * import androidx.compose.foundation.gestures.DraggableAnchors
 * import androidx.compose.foundation.layout.Arrangement
 * import androidx.compose.foundation.layout.Box
 * import androidx.compose.foundation.layout.Column
 * import androidx.compose.foundation.layout.Spacer
 * import androidx.compose.foundation.layout.WindowInsets
 * import androidx.compose.foundation.layout.fillMaxSize
 * import androidx.compose.foundation.layout.fillMaxWidth
 * import androidx.compose.foundation.layout.height
 * import androidx.compose.foundation.layout.offset
 * import androidx.compose.foundation.layout.padding
 * import androidx.compose.foundation.layout.size
 * import androidx.compose.foundation.layout.statusBars
 * import androidx.compose.foundation.pager.rememberPagerState
 * import androidx.compose.material3.BottomAppBar
 * import androidx.compose.material3.Button
 * import androidx.compose.material3.DrawerValue
 * import androidx.compose.material3.ExperimentalMaterial3Api
 * import androidx.compose.material3.HorizontalDivider
 * import androidx.compose.material3.Icon
 * import androidx.compose.material3.MaterialTheme
 * import androidx.compose.material3.ModalDrawerSheet
 * import androidx.compose.material3.ModalNavigationDrawer
 * import androidx.compose.material3.NavigationDrawerItem
 * import androidx.compose.material3.Scaffold
 * import androidx.compose.material3.SearchBarDefaults
 * import androidx.compose.material3.Text
 * import androidx.compose.material3.TextButton
 * import androidx.compose.material3.TopSearchBar
 * import androidx.compose.material3.rememberDrawerState
 * import androidx.compose.material3.rememberSearchBarState
 * import androidx.compose.runtime.Composable
 * import androidx.compose.runtime.derivedStateOf
 * import androidx.compose.runtime.getValue
 * import androidx.compose.runtime.mutableFloatStateOf
 * import androidx.compose.runtime.mutableIntStateOf
 * import androidx.compose.runtime.mutableStateOf
 * import androidx.compose.runtime.remember
 * import androidx.compose.runtime.rememberCoroutineScope
 * import androidx.compose.runtime.setValue
 * import androidx.compose.ui.Alignment
 * import androidx.compose.ui.Modifier
 * import androidx.compose.ui.draw.alpha
 * import androidx.compose.ui.input.nestedscroll.nestedScroll
 * import androidx.compose.ui.layout.onGloballyPositioned
 * import androidx.compose.ui.layout.positionInParent
 * import androidx.compose.ui.layout.positionInWindow
 * import androidx.compose.ui.platform.LocalContext
 * import androidx.compose.ui.platform.LocalDensity
 * import androidx.compose.ui.platform.LocalWindowInfo
 * import androidx.compose.ui.res.painterResource
 * import androidx.compose.ui.res.stringResource
 * import androidx.compose.ui.unit.dp
 * import androidx.lifecycle.compose.collectAsStateWithLifecycle
 * import androidx.lifecycle.viewmodel.compose.viewModel
 * import androidx.paging.compose.collectAsLazyPagingItems
 * import com.pointchange.audio.view.LocalNav
 * import com.pointchange.audio.R
 * import com.pointchange.audio.view.NavID
 * import com.pointchange.audio.model.AudioListViewModel
 * import com.pointchange.audio.model.PlayList
 * import com.pointchange.audio.model.SettingViewModel
 * import com.pointchange.audio.model.ThemeMode
 * import com.pointchange.audio.model_data.SortAudio
 * import com.pointchange.audio.service.VlcManager
 * import com.pointchange.audio.view.component.Cover
 * import com.pointchange.audio.view.component.DraggableBarContainer
 * import com.pointchange.audio.view.component.MusicControlBar
 * import com.pointchange.audio.view.component.MusicList
 * import com.pointchange.audio.view.component.PermissionRequest
 * import com.pointchange.audio.view.component.SearchTextField
 * import com.pointchange.audio.view.component.allFilesPermission
 * import kotlinx.coroutines.launch
 *
 *
 * @SuppressLint("FrequentlyChangingValue")
 * @OptIn(ExperimentalMaterial3Api::class)
 * @Composable
 * fun HomePage(
 *     audioListViewModel: AudioListViewModel = viewModel(),
 *     settingViewModel: SettingViewModel = viewModel(),
 * ) {
 *     val scrollBehavior = SearchBarDefaults.enterAlwaysSearchBarScrollBehavior()
 *     val searchBarState = rememberSearchBarState()
 *     val context = LocalContext.current
 *     val audioPages = audioListViewModel.sortAudio.collectAsLazyPagingItems()
 *     val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
 *     val scope = rememberCoroutineScope()
 *     val sortOrder by audioListViewModel.sortOrder.collectAsStateWithLifecycle()
 *
 *     val navActions = LocalNav.current
 *
 *     val windowInfo = LocalWindowInfo.current
 *     val density = LocalDensity.current
 *     val top = with(density) { WindowInsets.statusBars.getTop(density).toDp() }
 *     val screenHeightPx = with(density) { windowInfo.containerDpSize.height.toPx() }
 *     var scaffoldBottomBarPosition by remember { mutableFloatStateOf(with(density) { (64 + 24).dp.toPx() }) }
 *     val collapseOffset = screenHeightPx - scaffoldBottomBarPosition
 *     val draggableState = remember(collapseOffset) {
 *         AnchoredDraggableState(
 *             initialValue = PlayerDragValue.COLLAPSED,
 *         ).apply {
 *             updateAnchors(DraggableAnchors {
 *                 PlayerDragValue.EXPANDED at 0F
 *                 PlayerDragValue.COLLAPSED at (screenHeightPx - scaffoldBottomBarPosition)
 *             })
 *         }
 *     }
 *     val progress = remember(draggableState.offset) {
 *         if (draggableState.offset.isNaN()) 0F
 *         else draggableState.progress(
 *             from = PlayerDragValue.COLLAPSED,
 *             to = PlayerDragValue.EXPANDED
 *         )
 *     }
 *
 *     val tabs = listOf(stringResource(R.string.tab_0), stringResource(R.string.tab_1))
 *     val pagerState = rememberPagerState(pageCount = { tabs.size })
 *     val pagerProgressState by remember { derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction } }
 *
 *     var playerAndPlayProgress by remember { mutableFloatStateOf(0F) }
 *     val themeConfig by settingViewModel.themeConfig.collectAsStateWithLifecycle()
 * //    var scaffoldBottomBarPosition by remember { mutableStateOf(6.dp) }
 *
 *     Box(modifier = Modifier.fillMaxSize()) {
 *         Box(
 *             modifier = Modifier
 *                 .fillMaxSize()
 *                 .alpha(1F - (progress * 0.3F))
 *         ) {
 *             ModalNavigationDrawer(
 *                 drawerState = drawerState,
 *                 drawerContent = {
 *                     ModalDrawerSheet {
 *                         val themeModeList = ThemeMode.entries.toList()
 *                         var index by remember { mutableIntStateOf(themeConfig.themeMode.ordinal) }
 *                         Scaffold(
 *                             bottomBar = {
 *                                 TextButton(
 *                                     modifier = Modifier
 *                                         .fillMaxWidth()
 *                                         .padding(bottom = 64.dp),
 *                                     onClick = {
 *                                         val mode = themeModeList.getOrElse(++index) {
 *                                             index = 0
 *                                             themeModeList[index]
 *                                         }
 *                                         settingViewModel.setThemeMode(mode = mode)
 *                                     }) {
 *                                     Text(text = stringResource(themeConfig.themeMode.text))
 *                                 }
 *                             }
 *                         ) { padding ->
 *                             Column(modifier = Modifier.padding(padding)) {
 *                                 Text(
 *                                     text = stringResource(R.string.sort),
 *                                     modifier = Modifier.padding(16.dp),
 *                                     style = MaterialTheme.typography.titleMedium
 *                                 )
 *                                 SortAudio.entries.forEach { item ->
 *                                     NavigationDrawerItem(
 *                                         label = { Text(text = item.getString(context)) },
 *                                         icon = {
 *                                             if (sortOrder == item) {
 *                                                 Icon(
 *                                                     painter = painterResource(id = R.drawable.round_check_24),
 *                                                     contentDescription = null,
 *                                                 )
 *                                             }
 *                                         },
 *                                         selected = false,
 *                                         onClick = {
 *                                             val state = PlayList.valueOf(item.name)
 *                                             audioListViewModel.setSortOrder(item)
 *                                             audioListViewModel.setCurrentPlayListState(state = state)
 *                                             audioListViewModel.setPlayingInfo(state = state)
 *                                         }
 *                                     )
 *                                 }
 *
 *                                 HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
 *                                 Text(
 *                                     text = stringResource(R.string.list),
 *                                     modifier = Modifier.padding(16.dp),
 *                                     style = MaterialTheme.typography.titleMedium
 *                                 )
 *                                 NavigationDrawerItem(
 *                                     label = { Text(text = stringResource(R.string.favorite)) },
 *                                     selected = false,
 *                                     onClick = {
 *                                         audioListViewModel.setFavor(true)
 *                                         navActions?.add(NavID.Favorite)
 *                                     }
 *                                 )
 *
 *                                 HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
 *                                 Text(
 *                                     text = stringResource(R.string.more),
 *                                     modifier = Modifier.padding(16.dp),
 *                                     style = MaterialTheme.typography.titleMedium
 *                                 )
 *                                 NavigationDrawer.entries.forEach { item ->
 *                                     NavigationDrawerItem(
 *                                         label = { Text(text = item.getString(context)) },
 *                                         selected = false,
 *                                         onClick = {
 *                                             when (item) {
 *                                                 NavigationDrawer.SCAN -> {
 *                                                     navActions?.add(NavID.Scan)
 *                                                 }
 *
 *                                                 NavigationDrawer.SETTINGS -> {
 *                                                     navActions?.add(NavID.Setting)
 *
 *                                                 }
 *                                             }
 *                                         }
 *                                     )
 *                                 }
 *                             }
 *                         }
 *                     }
 *                 },
 *             ) {
 *                 Scaffold(
 *                     topBar = {
 *                         TopSearchBar(
 *                             state = searchBarState,
 *                             scrollBehavior = scrollBehavior,
 *                             inputField = {
 *                                 SearchTextField(searchBarState = searchBarState, drawerHandle = {
 *                                     scope.launch {
 *                                         drawerState.apply {
 *                                             if (isClosed) open() else close()
 *                                         }
 *                                     }
 *                                 })
 *                             }
 *                         )
 *                         SearchPage(searchBarState = searchBarState)
 *                     },
 *                     bottomBar = {
 * //                                MusicControlBar({ minBarHeightPx = it })
 *                         BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
 *                             Spacer(
 *                                 modifier = Modifier
 *                                     .height(64.dp)
 *                                     .onGloballyPositioned {
 *                                         with(density) {
 *                                             scaffoldBottomBarPosition =
 *                                                 (windowInfo.containerDpSize.height - it.positionInWindow().y.toDp()).toPx()
 *                                         }
 *                                     })
 *                         }
 *                     },
 *                     modifier = Modifier
 *                         .fillMaxSize()
 *                         .nestedScroll(scrollBehavior.nestedScrollConnection)
 *                 ) { innerPadding ->
 *                     Column(modifier = Modifier.padding(innerPadding)) {
 *                         if (audioPages.itemCount == 0) {
 *                             if (VlcManager.tempList.isEmpty()) {
 *                                 Column(
 *                                     modifier = Modifier.fillMaxSize(),
 *                                     verticalArrangement = Arrangement.Center,
 *                                     horizontalAlignment = Alignment.CenterHorizontally
 *                                 ) {
 *                                     Button(onClick = {
 *                                         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
 *                                             allFilesPermission(context.applicationContext) {
 *                                                 audioListViewModel.scanHandle(context.applicationContext)
 *                                             }
 *                                         } else {
 * //                                            Log.d("str123", "sdk<30")
 *                                         }
 *                                     }) {
 *                                         Text(stringResource(R.string.scan_local))
 *                                     }
 *                                 }
 *                             } else {
 *                                 //                        VlcManager.tempList.forEach { Text(text="${it.uri}") }
 *                                 MusicList(
 *                                     list = null,
 *                                     viewModel = audioListViewModel,
 *                                     tempList = VlcManager.tempList
 *                                 )
 *                                 //                        MusicList(list = flow{ emit(PagingData.from(VlcManager.tempList)) }.collectAsLazyPagingItems(), viewModel = audioListViewModel)
 *                             }
 *
 *                         } else {
 *                             MusicList(list = audioPages, viewModel = audioListViewModel)
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *         DraggableBarContainer(
 *             state = draggableState,
 *         ) {
 *             val barAlpha = if (progress > 0.6F) 0F else (1F - (progress / 0.6F))
 *             if (barAlpha > 0F) {
 *                 MusicControlBar(
 *                     draggableState = draggableState,
 *                     barAlpha = barAlpha
 *                 )
 *             }
 *             val playerAlpha = if (progress < 0.2F) 0F else ((progress - 0.2F) / 0.8F)
 *             //cover move
 *             //    start x 32 y 8
 *             //     end x16 y100
 *             val targetStartX = 32.dp
 *             var targetEndX by remember { mutableStateOf(16.dp) }
 *             val currentX = targetStartX + (targetEndX - targetStartX) * progress
 *
 *             val targetStartY = 6.dp
 *             var targetEndY =125.dp
 *             val currentY = targetStartY + (targetEndY - targetStartY) * progress
 *
 *             val backOriginX = currentX - (currentX - targetStartX/2) * playerAndPlayProgress
 *             val backOriginY = currentY - (currentY - targetStartY - top) * playerAndPlayProgress
 *
 *             var imageSize by remember { mutableStateOf(380.dp) }
 *             val currentImageSize =
 *                 50.dp + ((imageSize - 50.dp) * (if (playerAndPlayProgress > 0F) (1F - playerAndPlayProgress) else progress))
 *
 *
 *             if (progress > 0F) {
 *                 PlayerPage(
 *                     tabs = tabs,
 *                     pagerState = pagerState,
 *                     draggableState = draggableState,
 *                     barAlpha = playerAlpha,
 *                     screenHeightPx = screenHeightPx,
 *                     onSetTargetXY = { x, y, size ->
 *                         targetEndX = x
 *                         targetEndY = y
 *                         imageSize = size
 *                     }
 *                 ) {
 *                     playerAndPlayProgress = it
 *                 }
 *             }
 *
 *
 *             Cover(
 *                 pagerProgressState = pagerProgressState,
 *                 barAlpha = barAlpha,
 *                 context = context,
 *                 modifier = Modifier
 *                     .offset(
 *                         x = if (playerAndPlayProgress > 0F) backOriginX else currentX,
 *                         y = if (playerAndPlayProgress > 0F) backOriginY else currentY
 *                     )
 *                     .size(currentImageSize),
 *                 themeConfig = themeConfig,
 *                 audioListViewModel = audioListViewModel,
 *             ) {
 *                 audioListViewModel.setColorFromCover(it)
 *             }
 *         }
 *     }
 *
 *
 *
 *     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
 *         PermissionRequest(
 *             permission = Manifest.permission.READ_MEDIA_AUDIO,
 *             permissionText = stringResource(id = R.string.read_media_audio),
 *         ) {
 *         }
 *     } else {
 *         PermissionRequest(
 *             permission = Manifest.permission.READ_EXTERNAL_STORAGE,
 *             permissionText = stringResource(id = R.string.read_media_audio),
 *         ) {
 *         }
 *     }
 * }
 *
 *
 */

/**
 * player

 */

//package com.pointchange.audio.view.page
//
//import android.content.ClipData
//import android.content.Intent
//import android.util.Log
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.basicMarquee
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.gestures.AnchoredDraggableState
//import androidx.compose.foundation.gestures.DraggableAnchors
//import androidx.compose.foundation.gestures.animateTo
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.navigationBars
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.statusBars
//import androidx.compose.foundation.layout.windowInsetsPadding
//import androidx.compose.foundation.layout.wrapContentHeight
//import androidx.compose.foundation.pager.HorizontalPager
//import androidx.compose.foundation.pager.PagerState
//import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.BottomAppBarDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.ListItemDefaults
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.PrimaryTabRow
//import androidx.compose.material3.RadioButton
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Tab
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableFloatStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.layout.boundsInWindow
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.layout.positionInParent
//import androidx.compose.ui.layout.positionInWindow
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.platform.LocalWindowInfo
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.core.view.ViewCompat
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation3.ui.NavDisplay
//import com.pointchange.audio.R
//import com.pointchange.audio.model.AudioListViewModel
//import com.pointchange.audio.model.PlayerViewModel
//import com.pointchange.audio.model_data.PlayMode.LOOP
//import com.pointchange.audio.model_data.PlayMode.RANDOM
//import com.pointchange.audio.model_data.PlayMode.REPEAT
//import com.pointchange.audio.util.getUriFromPath
//import com.pointchange.audio.view.LocalNav
//import com.pointchange.audio.view.NavID
//import com.pointchange.audio.view.component.DraggableBarContainer
//import com.pointchange.audio.view.component.Lrc
//import com.pointchange.audio.view.component.PlayPauseBtn
//import com.pointchange.audio.view.component.ProgressAndText
//import com.pointchange.audio.view.component.SimplePlayList
//import kotlinx.coroutines.launch
//import java.io.File
//import kotlin.collections.List
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PlayerPage(
//    draggableState: AnchoredDraggableState<PlayerDragValue>,
//    barAlpha: Float,
//    tabs: List<String>,
//    pagerState: PagerState,
//    screenHeightPx: Float,
//    onSetTargetXY: (x: Dp, y: Dp, size: Dp) -> Unit,
//    audioListViewModel: AudioListViewModel = viewModel(),
//    playerViewModel: PlayerViewModel = viewModel(),
//    getPlayerAndPlayProgress: (progress: Float) -> Unit
//) {
//    val context = LocalContext.current
//    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
//    val lrcContent by audioListViewModel.lrcContent.collectAsStateWithLifecycle()
//
//    val backgroundColor by audioListViewModel.colorFromCover.collectAsStateWithLifecycle()
//    val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
//    val navActions = LocalNav.current
//    val playingAudioMetadata = playingInfo.audioMetadata
//    val uri = playingAudioMetadata?.uri ?: ""
//    val title = playingAudioMetadata?.title ?: ""
//    val artist = playingAudioMetadata?.artist ?: ""
//    val album = playingAudioMetadata?.album ?: ""
//    var favor by remember(playingAudioMetadata?.isFavorite) {
//        mutableStateOf(
//            playingAudioMetadata?.isFavorite ?: false
//        )
//    }
//    val textAndIconColor = Color.White
//
//    val shareText = stringResource(R.string.share_to)
//
//    var showMultipPlayBackBottomSheet by remember { mutableStateOf(false) }
//    val multipPlayBacksheetState = rememberModalBottomSheetState()
//    val rate by playerViewModel.multiplePlayBack.collectAsStateWithLifecycle()
//    val scope = rememberCoroutineScope()
//
//    val density = LocalDensity.current
//    val top = with(density) { WindowInsets.statusBars.getTop(density).toDp().toPx() }
//    var scaffoldBottomBarPosition by remember { mutableFloatStateOf(with(density) { (48 + 24).dp.toPx() }) }
//    val collapseOffset = screenHeightPx - scaffoldBottomBarPosition
//    val playerAndPlayListDraggableState = remember(collapseOffset) {
//        AnchoredDraggableState(
//            initialValue = PlayerDragValue.COLLAPSED,
//        ).apply {
//            updateAnchors(DraggableAnchors {
//                PlayerDragValue.EXPANDED at top
//                PlayerDragValue.COLLAPSED at collapseOffset
//            })
//        }
//    }
//    val playerAndPlayProgress = remember(playerAndPlayListDraggableState.offset) {
//        if (playerAndPlayListDraggableState.offset.isNaN()) 0F
//        else playerAndPlayListDraggableState.progress(
//            from = PlayerDragValue.COLLAPSED,
//            to = PlayerDragValue.EXPANDED
//        )
//    }
//    val playListAlpha =
//        if (playerAndPlayProgress < 0.2F) 0F else ((playerAndPlayProgress - 0.2F) / 0.8F)
//    LaunchedEffect(playerAndPlayProgress) {
//        getPlayerAndPlayProgress(playerAndPlayProgress)
//    }
//    val windowInfo = LocalWindowInfo.current
//
//    val appBarOffsetY = with(density) {
//        WindowInsets.navigationBars.getTop(density).toDp() + WindowInsets.navigationBars.getBottom(
//            density
//        ).toDp()
//    }
//    var imageSize by remember { mutableStateOf(380.dp) }
//    var imagePositionInParent by remember { mutableStateOf(125.dp) }
//    var topAppHeight by remember { mutableStateOf(88.dp) }
//
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Color(backgroundColor)
//            ),
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            Scaffold(
//                containerColor = audioListViewModel.transparencyColor,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .alpha(if (playListAlpha > 0F) (1F - playListAlpha) else barAlpha),
//                topBar = {
//                    TopAppBar(
//                        modifier = Modifier.onGloballyPositioned {
//                            with(density) {
//                                topAppHeight = it.size.height.toDp()
//                            }
//                        },
//                        colors = TopAppBarDefaults.topAppBarColors(containerColor = audioListViewModel.transparencyColor),
//                        navigationIcon = {
//                            IconButton(
//                                onClick = {
//                                    scope.launch {
//                                        draggableState.animateTo(PlayerDragValue.COLLAPSED)
//                                    }
//                                }) {
//                                Icon(
//                                    painter = painterResource(R.drawable.round_keyboard_arrow_down_24),
//                                    contentDescription = stringResource(R.string.back),
//                                    tint = textAndIconColor
//                                )
//                            }
//                        },
//                        title = {
//                            PrimaryTabRow(
//                                containerColor = Color.Transparent,
//                                selectedTabIndex = pagerState.currentPage,
//                                divider = {},
//                                indicator = {
//                                    Box(
//                                        modifier = Modifier
//                                            .tabIndicatorOffset(pagerState.currentPage)
//                                            .background(Color.White.copy(alpha = 0.12F))
//                                            .height(1.dp)
//                                    ) {}
//                                }
//                            ) {
//                                tabs.forEachIndexed { index, title ->
//                                    Tab(
//                                        selectedContentColor = textAndIconColor,
//                                        selected = pagerState.currentPage == index,
//                                        onClick = {
//                                            scope.launch {
//                                                pagerState.animateScrollToPage(
//                                                    index
//                                                )
//                                            }
//                                        }
//                                    ) {
//                                        Text(text = title, color = textAndIconColor)
//                                    }
//                                }
//                            }
//                        },
//                        actions = {
//                            IconButton(onClick = { navActions?.add(NavID.AudioEffect) }) {
//                                Icon(
//                                    painter = painterResource(R.drawable.round_tune_24),
//                                    contentDescription = stringResource(R.string.tone_control),
//                                    tint = textAndIconColor
//                                )
//                            }
//                        })
//                },
//                bottomBar = {
//                    Surface(
//                        color = audioListViewModel.transparencyColor,
//                        modifier = Modifier
//                            .wrapContentHeight()
//                            .windowInsetsPadding(
//                                WindowInsets.navigationBars
//                            )
//                    ) {
//                        Column() {
//                            ProgressAndText(
//                                currentTime = playerViewModel.current,
//                                duration = playerViewModel.duration,
//                                onValueChangeFinished = {
//                                    playerViewModel.seekTo(it)
//                                },
//                                textColors = textAndIconColor,
//                                listItemColors = ListItemDefaults.colors(containerColor = audioListViewModel.transparencyColor)
//                            )
//
//                            ListItem(
//                                leadingContent = {
//                                    IconButton(onClick = { audioListViewModel.setPlayingInfoMode() }) {
//                                        Icon(
//                                            painter = painterResource(
//                                                id = when (playingInfo.mode) {
//                                                    LOOP -> R.drawable.round_repeat_24
//                                                    REPEAT -> R.drawable.round_repeat_one_24
//                                                    RANDOM -> R.drawable.round_shuffle_24
//                                                }
//                                            ),
//                                            contentDescription = playingInfo.mode.getString(context),
//                                            tint = textAndIconColor
//                                        )
//                                    }
//                                },
//                                headlineContent = {
//                                    Row(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        horizontalArrangement = Arrangement.Center,
//                                        verticalAlignment = Alignment.CenterVertically,
//                                    ) {
//                                        IconButton(
//                                            onClick = { audioListViewModel.previous() },
//                                            modifier = Modifier.size(50.dp)
//                                        ) {
//                                            Icon(
//                                                painter = painterResource(R.drawable.round_skip_previous_24),
//                                                contentDescription = null,
//                                                modifier = Modifier.size(50.dp),
//                                                tint = textAndIconColor
//                                            )
//                                        }
//                                        PlayPauseBtn(
//                                            isPlaying = isPlaying,
//                                            onClick = {
//                                                if (audioListViewModel.playList.isEmpty()) return@PlayPauseBtn
//                                                if (isPlaying) playerViewModel.pause() else playerViewModel.play()
//                                            },
//                                            tint = textAndIconColor,
//                                            modifier = Modifier.size(80.dp)
//                                        )
//
//                                        IconButton(
//                                            onClick = {
//                                                audioListViewModel.next()
//                                            },
//                                            modifier = Modifier.size(50.dp)
//                                        ) {
//                                            Icon(
//                                                painter = painterResource(R.drawable.round_skip_next_24),
//                                                contentDescription = null,
//                                                modifier = Modifier.size(50.dp),
//                                                tint = textAndIconColor
//                                            )
//                                        }
//                                    }
//                                },
//                                trailingContent = {
//                                    IconButton(onClick = {
//                                        audioListViewModel.updateFavoriteState()
//                                        favor = !favor
//                                    }) {
//                                        Icon(
//                                            painter = painterResource(if (favor) R.drawable.round_favorite_24 else R.drawable.round_favorite_border_24),
//                                            contentDescription = null,
//                                            tint = textAndIconColor
//                                        )
//                                    }
//                                },
//                                colors = ListItemDefaults.colors(containerColor = audioListViewModel.transparencyColor)
//                            )
//                            //189.0.dp
//                            Spacer(
//                                modifier = Modifier
//                                    .height(48.dp)
//                                    .onGloballyPositioned {
//                                        with(density) {
//                                            scaffoldBottomBarPosition =
//                                                (windowInfo.containerDpSize.height - it.positionInWindow().y.toDp()).toPx()
//                                        }
//                                    })
////                            Row(
////                                modifier = Modifier
////                                    .height(48.dp)
////                                    .fillMaxWidth()
////                                    .background(Color.White)
////                                    .onGloballyPositioned {
////                                        Log.d("str123", "control: ${it.positionInWindow().y}")
////                                    }) { Text("str") }
//                        }
//                    }
//                }
//            ) { paddingValues ->
//
//                Column(
//                    modifier = Modifier
//                        .padding(paddingValues),
//                    verticalArrangement = Arrangement.SpaceBetween
//                ) {
//                    HorizontalPager(state = pagerState) { page ->
//                        when (page) {
//                            0 -> {
//                                Column() {
//
//                                    Row(
//                                        modifier = Modifier
//                                            .padding(horizontal = 16.dp)
//                                            .fillMaxWidth()
//                                            .weight(1F)
//                                            .onGloballyPositioned {
//                                                with(density) {
//                                                    val h = it.size.height.toDp()
//                                                    val w = it.size.width.toDp()
//                                                    imageSize = if (w.toPx() > h.toPx()) {
//                                                        h
//                                                    } else {
//                                                        w
//                                                    }
//                                                }
//                                            },
//                                        horizontalArrangement = Arrangement.Center,
//                                        verticalAlignment = Alignment.CenterVertically
//                                    ) {
//                                        Image(
//                                            painter = painterResource(R.drawable.round_music_note_24),
//                                            contentDescription = null,
//                                            modifier = Modifier
//                                                .size(imageSize)
//                                                .alpha(0F)
//                                                .onGloballyPositioned {
//                                                    with(density) {
//                                                        imagePositionInParent =
//                                                            it.positionInParent().y.toDp()
//
//                                                        onSetTargetXY(
//                                                            it.positionInWindow().x.toDp(),
//                                                            imagePositionInParent+topAppHeight,
//                                                            imageSize
//                                                        )
//                                                    }
//                                                }
//                                        )
//                                    }
//
//                                    ListItem(
//                                        modifier = Modifier.fillMaxWidth(),
//                                        colors = ListItemDefaults.colors(containerColor = audioListViewModel.transparencyColor),
//                                        overlineContent = {
//
//                                        },
//                                        headlineContent = {
//                                            Text(
//                                                text = title,
//                                                maxLines = 1,
//                                                modifier = Modifier
//                                                    .padding(top = 16.dp)
//                                                    .basicMarquee(),
//                                                style = MaterialTheme.typography.headlineLarge,
//                                                color = textAndIconColor
//                                            )
//                                        },
//                                        supportingContent = {
//                                            Column {
//                                                if (playingAudioMetadata?.artist != null) {
//                                                    Text(
//                                                        text = artist,
//                                                        maxLines = 1,
//                                                        overflow = TextOverflow.Ellipsis,
//                                                        style = MaterialTheme.typography.titleSmall,
//                                                        color = textAndIconColor
//                                                    )
//                                                }
//                                                if (playingAudioMetadata?.album != null) {
//                                                    Text(
//                                                        text = album,
//                                                        maxLines = 1,
//                                                        overflow = TextOverflow.Ellipsis,
//                                                        style = MaterialTheme.typography.titleSmall,
//                                                        color = textAndIconColor
//
//                                                    )
//                                                }
//                                            }
//
//                                        }
//                                    )
//                                }
//                            }
//
//                            1 -> {
//                                if (lrcContent.isNotEmpty()) {
//                                    Lrc(
//                                        lrcContent = lrcContent,
//                                        current = playerViewModel.current,
//                                        onSeekTo = {
//                                            playerViewModel.seekTo(it.toFloat())
//                                        }
//                                    )
//                                } else {
//                                    Column(
//                                        verticalArrangement = Arrangement.Center,
//                                        horizontalAlignment = Alignment.CenterHorizontally,
//                                        modifier = Modifier.fillMaxSize()
//                                    ) {
//                                        TextButton(onClick = {
//                                            navActions?.add(NavID.ScanLrc)
//                                        }) {
//                                            Text(stringResource(R.string.lrc_input))
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                    }
//
//                }
//
//
//                if (showMultipPlayBackBottomSheet) {
//                    ModalBottomSheet(
//                        onDismissRequest = {
//                            showMultipPlayBackBottomSheet = false
//                        },
//                        sheetState = multipPlayBacksheetState
//                    ) {
//                        MultiplePlayBack.entries.forEach {
//                            ListItem(
//                                headlineContent = { Text(text = it.text + "x") },
//                                trailingContent = {
//                                    RadioButton(
//                                        selected = (it.text.toFloat() == rate),
//                                        onClick = null
//                                    )
//                                },
//                                modifier = Modifier.clickable(onClick = {
//                                    playerViewModel.setMultiplePlayBack(
//                                        it.text.toFloat()
//                                    )
//                                })
//                            )
//                        }
//                    }
//                }
//            }
//        }
//        DraggableBarContainer(
//            state = playerAndPlayListDraggableState,
//        ) {
//
//
//            val rotate by animateFloatAsState(
//                targetValue = if (playerAndPlayProgress == 1F) 180F else 0F,
//                animationSpec = tween(durationMillis = 600),
//                label = "playerControl",
//            )
//
//
//            //48+24 = 72
//            Box(
//                contentAlignment = Alignment.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .then(
//                        if (playerAndPlayProgress > 0F) {
//                            Modifier
//                                .height(48.dp + (64.dp - 48.dp) * playerAndPlayProgress + appBarOffsetY * (1F - playerAndPlayProgress))
//                                .background(
//                                    color = Color(backgroundColor).copy(alpha = playListAlpha),
//                                )
//
//                        } else {
//                            Modifier
//                        }
//                    )
//            ) {
//                Box(contentAlignment = Alignment.Center, modifier = Modifier.graphicsLayer {
//                    rotationX = rotate
//                    cameraDistance = 12F * this.density
//                }) {
//                    if (rotate <= 90F) {
//                        Row(
//                            modifier = Modifier
//                                .background(
//                                    color = Color.White.copy(alpha = 0.12F),
//                                    shape = RoundedCornerShape(10.dp)
//                                )
//                        ) {
//                            TextButton(onClick = {
//                                //onChangeShowLrc()
//                                navActions?.add(NavID.ScanLrc)
//                            }) {
//                                Text(
//                                    text = stringResource(R.string.lrc),
//                                    color = textAndIconColor
//                                )
//                            }
//                            PlayerOtherOperation.entries.forEach {
//                                IconButton(onClick = {
//                                    when (it) {
//                                        PlayerOtherOperation.SHARE -> {
//                                            val file = File(uri)
//                                            val uri =
//                                                getUriFromPath(context, file)
//                                                    ?: return@IconButton
//                                            Intent(Intent.ACTION_SEND).apply {
//                                                type = "*/*"
//                                                putExtra(Intent.EXTRA_STREAM, uri)
//                                                clipData =
//                                                    ClipData.newRawUri(
//                                                        file.nameWithoutExtension,
//                                                        uri
//                                                    )
//                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                                                context.startActivity(
//                                                    Intent.createChooser(
//                                                        this,
//                                                        shareText
//                                                    )
//                                                )
//                                            }
//                                        }
//
//                                        PlayerOtherOperation.INFO -> {
//                                            audioListViewModel.parseAudioInfo(uri)
//                                            navActions?.add(NavID.Information)
//                                        }
//
//                                        PlayerOtherOperation.MULTIPLE_PLAYBACK -> {
//                                            showMultipPlayBackBottomSheet = true
//                                        }
//                                    }
//                                }) {
//                                    Icon(
//                                        painter = painterResource(it.icon),
//                                        contentDescription = stringResource(it.text),
//                                        tint = textAndIconColor
//                                    )
//                                }
//
//                            }
//                        }
//                    } else {
//                        Box(modifier = Modifier.graphicsLayer { rotationX = 180F }) {
//                            ListItem(
//                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
//                                leadingContent = {
//                                    Spacer(modifier = Modifier.size(50.dp))
//                                },
//                                headlineContent = {
//                                    Text(
//                                        text = title,
//                                        overflow = TextOverflow.Ellipsis,
//                                        maxLines = 1,
//                                        style = MaterialTheme.typography.titleMedium,
//                                        color = textAndIconColor
//                                    )
//                                },
//                                supportingContent = {
//                                    if (artist != "") {
//                                        Text(
//                                            text = artist,
//                                            overflow = TextOverflow.Ellipsis,
//                                            maxLines = 1,
//                                            style = MaterialTheme.typography.titleSmall,
//                                            color = textAndIconColor
//                                        )
//                                    }
//                                },
//                                trailingContent = {
//                                    PlayPauseBtn(
//                                        isPlaying = isPlaying,
//                                        onClick = {
//                                            if (audioListViewModel.playList.isEmpty()) return@PlayPauseBtn
//                                            if (isPlaying) playerViewModel.pause() else playerViewModel.play()
//                                        },
//                                        tint = textAndIconColor,
//                                    )
//                                },
//                            )
//                        }
//
//                    }
//
//
//                }
//            }
//
//            if (playerAndPlayProgress > 0F) {
//                SimplePlayList(
//                    list = audioListViewModel.playList,
//                    playListAlpha = playListAlpha,
//                    backgroundColor = backgroundColor,
//                    playerAndPlayListDraggableState = playerAndPlayListDraggableState,
//                    onPlay = { audioListViewModel.playHandle(it) }
//                ) { from, to ->
//                    audioListViewModel.movePlayListItem(from, to)
//                }
//            }
//
//
//        }
//
//
//    }
//}


/**
 * bar
 *
 * package com.pointchange.audio.view.component
 *
 * import android.util.Log
 * import androidx.compose.foundation.background
 * import androidx.compose.foundation.basicMarquee
 * import androidx.compose.foundation.clickable
 * import androidx.compose.foundation.gestures.AnchoredDraggableState
 * import androidx.compose.foundation.gestures.animateTo
 * import androidx.compose.foundation.layout.Arrangement
 * import androidx.compose.foundation.layout.Box
 * import androidx.compose.foundation.layout.Row
 * import androidx.compose.foundation.layout.Spacer
 * import androidx.compose.foundation.layout.fillMaxHeight
 * import androidx.compose.foundation.layout.fillMaxWidth
 * import androidx.compose.foundation.layout.height
 * import androidx.compose.foundation.layout.padding
 * import androidx.compose.foundation.layout.size
 * import androidx.compose.foundation.lazy.LazyColumn
 * import androidx.compose.foundation.lazy.rememberLazyListState
 * import androidx.compose.foundation.shape.RoundedCornerShape
 * import androidx.compose.material3.AlertDialog
 * import androidx.compose.material3.CircularProgressIndicator
 * import androidx.compose.material3.ExperimentalMaterial3Api
 * import androidx.compose.material3.Icon
 * import androidx.compose.material3.IconButton
 * import androidx.compose.material3.ListItem
 * import androidx.compose.material3.ListItemDefaults
 * import androidx.compose.material3.MaterialTheme
 * import androidx.compose.material3.ModalBottomSheet
 * import androidx.compose.material3.Text
 * import androidx.compose.material3.TextButton
 * import androidx.compose.material3.rememberModalBottomSheetState
 * import androidx.compose.runtime.Composable
 * import androidx.compose.runtime.getValue
 * import androidx.compose.runtime.mutableStateOf
 * import androidx.compose.runtime.remember
 * import androidx.compose.runtime.rememberCoroutineScope
 * import androidx.compose.runtime.setValue
 * import androidx.compose.ui.Alignment
 * import androidx.compose.ui.Modifier
 * import androidx.compose.ui.draw.alpha
 * import androidx.compose.ui.draw.clip
 * import androidx.compose.ui.draw.scale
 * import androidx.compose.ui.graphics.Brush
 * import androidx.compose.ui.graphics.Color
 * import androidx.compose.ui.platform.LocalContext
 * import androidx.compose.ui.res.painterResource
 * import androidx.compose.ui.res.stringResource
 * import androidx.compose.ui.text.style.TextOverflow
 * import androidx.compose.ui.unit.dp
 * import androidx.compose.ui.unit.sp
 * import androidx.lifecycle.compose.collectAsStateWithLifecycle
 * import androidx.lifecycle.viewmodel.compose.viewModel
 * import com.pointchange.audio.R
 * import com.pointchange.audio.model.AudioListViewModel
 * import com.pointchange.audio.model.PlayList.ARTIST
 * import com.pointchange.audio.model.PlayList.DEFAULT
 * import com.pointchange.audio.model.PlayList.FAVORITE
 * import com.pointchange.audio.model.PlayList.SEARCH
 * import com.pointchange.audio.model.PlayList.TITLE
 * import com.pointchange.audio.model.PlayerViewModel
 * import com.pointchange.audio.model_data.PlayMode.LOOP
 * import com.pointchange.audio.model_data.PlayMode.RANDOM
 * import com.pointchange.audio.model_data.PlayMode.REPEAT
 * import com.pointchange.audio.view.page.PlayerDragValue
 * import kotlinx.coroutines.launch
 * import sh.calvin.reorderable.ReorderableItem
 * import sh.calvin.reorderable.rememberReorderableLazyListState
 * import java.io.File
 * import kotlin.coroutines.cancellation.CancellationException
 *
 * @OptIn(ExperimentalMaterial3Api::class)
 * @Composable
 * fun MusicControlBar(
 *     draggableState: AnchoredDraggableState<PlayerDragValue>,
 *     barAlpha: Float,
 *     audioListViewModel: AudioListViewModel = viewModel(),
 *     playerViewModel: PlayerViewModel = viewModel(),
 * ) {
 *     val scope = rememberCoroutineScope()
 *     val context = LocalContext.current
 *     val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
 *     val playingAudioMetadata = playingInfo.audioMetadata
 *     val title = playingAudioMetadata?.title
 *         ?: playingAudioMetadata?.uri?.substringAfterLast("/")?.substringBeforeLast(".") ?: ""
 *     val artist = playingAudioMetadata?.artist
 *         ?: ""
 *     val current by playerViewModel.current.collectAsStateWithLifecycle()
 *     val duration by playerViewModel.duration.collectAsStateWithLifecycle()
 *     val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
 *     var showBottomSheet by remember { mutableStateOf(false) }
 *     val sheetState = rememberModalBottomSheetState(
 *         skipPartiallyExpanded = false,
 *     )
 *     val backgroundColor by audioListViewModel.colorFromCover.collectAsStateWithLifecycle()
 *     var showClearDialog by remember { mutableStateOf(false) }
 *
 *     val boxModifier=remember(barAlpha) {
 *         if(barAlpha<1F){
 *             Modifier
 *                 .height(300.dp)
 *                 .background(Color(backgroundColor))
 *         }else{
 *             Modifier
 *         }
 *     }
 *
 * Box(
 *     modifier = boxModifier
 *
 * ){
 *     ListItem(
 *         modifier=if(barAlpha<1F){
 *             Modifier
 *                 .background(Color.Transparent).alpha(barAlpha)
 *         }else{
 *             Modifier
 *                 .padding(horizontal = 16.dp)
 *                 .height(64.dp)
 *                 .clip(shape = RoundedCornerShape(6.dp))
 *                 .alpha(barAlpha)
 *                 .background(
 *                     Brush.horizontalGradient(
 *                         colors = listOf(
 *                             Color(backgroundColor).copy(alpha = 0.12f),
 *                             Color(backgroundColor).copy(alpha = 0.4f),
 *                         )
 *                     )
 *                 )
 *         },
 *         colors = ListItemDefaults.colors(
 *             containerColor =
 *                 if (playingAudioMetadata?.coverPath != null || barAlpha<1F) {
 *                     Color.Transparent
 *                 } else {
 *                     MaterialTheme.colorScheme.surfaceContainerHigh
 *                 }
 *         ),
 *         headlineContent = {
 *             Text(
 *                 text = title,
 *                 fontSize = 20.sp,
 *                 maxLines = 1,
 *                 modifier = Modifier.basicMarquee(),
 *             )
 *         },
 *         supportingContent = {
 *             if (artist != "") {
 *                 Text(
 *                     text = artist,
 *                     overflow = TextOverflow.Ellipsis,
 *                     maxLines = 1
 *                 )
 *             }
 *         },
 *         leadingContent = {
 *             //Color(0xFF666666)
 *             Spacer(
 *                 modifier = Modifier.size(50.dp)
 *             )
 *         },
 *         trailingContent = {
 *             Row {
 *                 Box(contentAlignment = Alignment.Center) {
 *                     PlayPauseBtn(
 *                         isPlaying = isPlaying,
 *                         onClick = {
 *                             if (audioListViewModel.playList.isEmpty()) return@PlayPauseBtn
 *                             if (isPlaying) playerViewModel.pause() else playerViewModel.play()
 *                         },
 *                         modifier = Modifier.scale(1.15F)
 *                     )
 *                     CircularProgressIndicator(progress = {
 *                         if (duration == 0L) {
 *                             0F
 *                         } else {
 *                             current / duration.toFloat()
 *                         }
 *                     })
 *                 }
 *                 IconButton(onClick = {
 *                     showBottomSheet = true
 *                 }) {
 *                     Icon(
 *                         painter = painterResource(R.drawable.baseline_format_list_bulleted_24),
 *                         contentDescription = stringResource(R.string.playing_music_list)
 *                     )
 *                 }
 *             }
 *         },
 *     )
 * }
 *
 *
 *     if (showBottomSheet) {
 *         val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
 *         val list = audioListViewModel.playList
 *         val lazyListState = rememberLazyListState()
 *         val reorderableLazyListState =
 *             rememberReorderableLazyListState(lazyListState) { from, to ->
 *                 audioListViewModel.movePlayListItem(from.index, to.index)
 *
 *             }
 *         val text = playingInfo.mode.getString(context)
 *
 *         ModalBottomSheet(
 *             modifier = Modifier.fillMaxHeight(),
 *             sheetState = sheetState,
 *             onDismissRequest = { showBottomSheet = false }
 *         ) {
 *             ListItem(
 *                 leadingContent = {
 *                     Icon(
 *                         painter = painterResource(R.drawable.round_close_24),
 *                         contentDescription = stringResource(R.string.close),
 *                         modifier = Modifier.clickable(onClick = { showBottomSheet = false })
 *                     )
 *                 },
 *                 headlineContent = {},
 *                 trailingContent = {
 *                     Icon(
 *                         painter = painterResource(R.drawable.round_delete_sweep_24),
 *                         contentDescription = stringResource(R.string.clear_play_list),
 *                         modifier = Modifier.clickable(onClick = { showClearDialog = true })
 *                     )
 *
 *                 }
 *             )
 *             ListItem(leadingContent = {
 *                 Icon(
 *                     painter = when (playingInfo.currentList) {
 *                         DEFAULT, TITLE, ARTIST -> painterResource(R.drawable.round_music_note_24)
 *                         SEARCH -> painterResource(R.drawable.round_search_24)
 *                         FAVORITE -> painterResource(R.drawable.round_favorite_24)
 *                     },
 *                     contentDescription = stringResource(R.string.list),
 *                 )
 *             }, headlineContent = {
 *                 Row(
 *                     horizontalArrangement = Arrangement.SpaceBetween,
 *                     verticalAlignment = Alignment.CenterVertically,
 *                     modifier = Modifier.fillMaxWidth()
 *                 ) {
 *                     Row(
 *                         horizontalArrangement = Arrangement.Center,
 *                         verticalAlignment = Alignment.CenterVertically,
 *                     ) {
 *                         Text(
 *                             text = when (playingInfo.currentList) {
 *                                 DEFAULT, TITLE, ARTIST -> stringResource(
 *                                     R.string.default_list_queue
 *                                 )
 *
 *                                 SEARCH -> stringResource(R.string.search_queue)
 *                                 FAVORITE -> stringResource(R.string.favorite_queue)
 *                             },
 *                             overflow = TextOverflow.Ellipsis,
 *                             maxLines = 1,
 *                             style = MaterialTheme.typography.titleMedium
 *                         )
 *                         Text(
 *                             text = " ${list.size} ${stringResource(R.string.song_count)}",
 *                             overflow = TextOverflow.Ellipsis,
 *                             maxLines = 1,
 *                             color = MaterialTheme.colorScheme.secondary
 *                         )
 *                     }
 *
 *                     Text(text = text, style = MaterialTheme.typography.titleMedium)
 *                 }
 *
 *             }, trailingContent = {
 *                 Icon(
 *                     painter = painterResource(
 *                         id = when (playingInfo.mode) {
 *                             LOOP -> R.drawable.round_repeat_24
 *                             REPEAT -> R.drawable.round_repeat_one_24
 *                             RANDOM -> R.drawable.round_shuffle_24
 *                         }
 *                     ),
 *                     contentDescription = text
 *                 )
 *             })
 *
 *             LazyColumn(state = lazyListState) {
 *                 items(count = list.size, key = { index -> list[index].uri }) { index ->
 *                     val item = list[index]
 *                     val title = item.title ?: File(item.uri).name
 *                     val color =
 *                         if (index == playingInfo.index) MaterialTheme.colorScheme.primary else Color.Unspecified
 *
 *                     ReorderableItem(
 *                         state = reorderableLazyListState,
 *                         key = item.uri
 *                     ) { isDragging ->
 *                         Box(modifier = Modifier.longPressDraggableHandle()) {
 *                             ListItem(
 *                                 leadingContent = {
 *                                     Icon(
 *                                         painter = painterResource(R.drawable.round_drag_handle_24),
 *                                         contentDescription = stringResource(R.string.drag),
 *                                         modifier = Modifier.draggableHandle()
 *                                     )
 *                                 },
 *                                 headlineContent = {
 *                                     Text(
 *                                         text = title,
 *                                         overflow = TextOverflow.Ellipsis,
 *                                         maxLines = 1,
 *                                         style = MaterialTheme.typography.titleMedium,
 *                                         color = color
 *                                     )
 *                                 },
 *                                 supportingContent = {
 *                                     if (item.artist != null) {
 *                                         Text(
 *                                             text = item.artist,
 *                                             overflow = TextOverflow.Ellipsis,
 *                                             maxLines = 1,
 *                                             style = MaterialTheme.typography.titleSmall,
 *                                             color = color
 *                                         )
 *                                     }
 *                                 },
 *                                 trailingContent = {
 *                                     Icon(
 *                                         painter = painterResource(R.drawable.round_close_24),
 *                                         contentDescription = stringResource(R.string.clear_an_item),
 *                                         modifier = Modifier.clickable(onClick = {
 *
 *                                             audioListViewModel.removeAtPlayListItem(index).also {
 *                                                 if (audioListViewModel.playList.isEmpty()) {
 *                                                     playerViewModel.pause()
 *                                                     audioListViewModel.clearPlayingInfo()
 *                                                 }
 *                                             }
 *                                         })
 *                                     )
 *                                 },
 *                                 modifier = Modifier.clickable(onClick = {
 *                                     audioListViewModel.playHandle(
 *                                         item.uri
 *                                     )
 *                                 })
 *                             )
 *                         }
 *                     }
 *                 }
 *
 *             }
 *         }
 *     }
 *
 *     if (showClearDialog) {
 *         AlertDialog(
 *             onDismissRequest = { showClearDialog = false },
 *             title = { Text(text = stringResource(R.string.clear_dialog_content)) },
 *             confirmButton = {
 *                 TextButton(onClick = {
 *                     playerViewModel.pause()
 *                     audioListViewModel.clearPlayList()
 *                     audioListViewModel.clearPlayingInfo()
 *                     showClearDialog = false
 *                 }) {
 *                     Text(
 *                         text = stringResource(
 *                             R.string.clear
 *                         )
 *                     )
 *                 }
 *             },
 *             dismissButton = {
 *                 TextButton(onClick = {
 *                     showClearDialog = false
 *                 }) { Text(text = stringResource(R.string.cancel)) }
 *             },
 *         )
 *     }
 * }
 *
 */

/**
 * cover
 *
 * package com.pointchange.audio.view.component
 *
 * import android.content.Context
 * import android.graphics.Bitmap
 * import androidx.compose.foundation.isSystemInDarkTheme
 * import androidx.compose.foundation.layout.Box
 * import androidx.compose.foundation.layout.fillMaxWidth
 * import androidx.compose.foundation.shape.RoundedCornerShape
 * import androidx.compose.runtime.Composable
 * import androidx.compose.runtime.getValue
 * import androidx.compose.runtime.mutableStateOf
 * import androidx.compose.runtime.remember
 * import androidx.compose.runtime.setValue
 * import androidx.compose.ui.Modifier
 * import androidx.compose.ui.draw.alpha
 * import androidx.compose.ui.draw.clip
 * import androidx.compose.ui.graphics.Color
 * import androidx.compose.ui.graphics.graphicsLayer
 * import androidx.compose.ui.layout.ContentScale
 * import androidx.compose.ui.platform.LocalDensity
 * import androidx.compose.ui.platform.LocalWindowInfo
 * import androidx.compose.ui.unit.dp
 * import androidx.core.graphics.drawable.toBitmap
 * import androidx.lifecycle.compose.collectAsStateWithLifecycle
 * import coil.compose.AsyncImage
 * import coil.request.ImageRequest
 * import com.pointchange.audio.model.AudioListViewModel
 * import com.pointchange.audio.model.ThemeConfig
 * import com.pointchange.audio.view.state.rememberIsDark
 *
 * @Composable
 * fun Cover(
 *     modifier: Modifier = Modifier,
 *     context: Context,
 *     barAlpha: Float,
 *     pagerProgressState: Float,
 *     audioListViewModel: AudioListViewModel,
 *     themeConfig: ThemeConfig,
 *     setColorFromCover: (bitmap: Bitmap?) -> Unit,
 * ) {
 *     var loadingCoverError by remember { mutableStateOf(false) }
 *     val playingInfo by audioListViewModel.playingInfo.collectAsStateWithLifecycle()
 *     val isDark= rememberIsDark(themeConfig = themeConfig)
 *     Box(modifier = modifier) {
 *         if (playingInfo.audioMetadata != null && playingInfo.audioMetadata?.coverPath != "") {
 *             AsyncImage(
 *                 model = ImageRequest.Builder(context)
 *                     .data(playingInfo.audioMetadata?.coverPath)
 *                     .allowHardware(false)
 *                     .size(380)
 *                     .crossfade(true)
 *                     .build(),
 *                 contentDescription = null,
 *                 contentScale = ContentScale.Fit,
 *                 modifier = Modifier
 *                     .fillMaxWidth()
 *                     .clip(shape = RoundedCornerShape(10.dp))
 *                     .then(
 *                         if (barAlpha < 1F) {
 *                             Modifier
 *                                 .alpha(1F - pagerProgressState)
 *                                 .graphicsLayer {
 *                                     translationX = -380 * pagerProgressState
 *                                 }
 *                         } else {
 *                             Modifier
 *                         }
 *                     ),
 *                 onLoading = {
 *                     loadingCoverError = false
 *                 },
 *                 onSuccess = { state ->
 *                     val bitmap = state.result.drawable.toBitmap()
 *                     setColorFromCover(bitmap)
 *                 },
 *                 onError = {
 *                     loadingCoverError = true
 *                 }
 *             )
 *         }
 *         ////                            mostColor = Color(0xFF666666),
 *         ////                            remainColor = Color.DarkGray,
 *         if (loadingCoverError) {
 *
 *             if (barAlpha < 1F || isDark) {
 *                 Box(
 *                     modifier = Modifier
 *                         .alpha(1F - pagerProgressState)
 *                         .graphicsLayer {
 *                             translationX = -380 * pagerProgressState
 *                         }) {
 *                     AudioCover(
 *                         mostColor = Color(0xFF444444),
 *                         remainColor = Color(0xFF222222)
 *                     )
 *                 }
 *             } else {
 *                 AudioCover()
 *             }
 *             setColorFromCover(null)
 *         }
 *
 *     }
 * }
 *
 *
 */

/**
 * list
 *
 * package com.pointchange.audio.view.component
 *
 * import androidx.compose.foundation.background
 * import androidx.compose.foundation.clickable
 * import androidx.compose.foundation.gestures.AnchoredDraggableState
 * import androidx.compose.foundation.gestures.animateTo
 * import androidx.compose.foundation.isSystemInDarkTheme
 * import androidx.compose.foundation.layout.Box
 * import androidx.compose.foundation.layout.fillMaxSize
 * import androidx.compose.foundation.layout.fillMaxWidth
 * import androidx.compose.foundation.layout.padding
 * import androidx.compose.foundation.layout.size
 * import androidx.compose.foundation.lazy.LazyColumn
 * import androidx.compose.foundation.lazy.rememberLazyListState
 * import androidx.compose.foundation.shape.RoundedCornerShape
 * import androidx.compose.material3.FloatingActionButton
 * import androidx.compose.material3.Icon
 * import androidx.compose.material3.ListItem
 * import androidx.compose.material3.ListItemDefaults
 * import androidx.compose.material3.MaterialTheme
 * import androidx.compose.material3.Scaffold
 * import androidx.compose.material3.Text
 * import androidx.compose.runtime.Composable
 * import androidx.compose.runtime.rememberCoroutineScope
 * import androidx.compose.runtime.snapshots.SnapshotStateList
 * import androidx.compose.ui.Modifier
 * import androidx.compose.ui.draw.alpha
 * import androidx.compose.ui.draw.clip
 * import androidx.compose.ui.graphics.Color
 * import androidx.compose.ui.graphics.graphicsLayer
 * import androidx.compose.ui.graphics.toArgb
 * import androidx.compose.ui.res.painterResource
 * import androidx.compose.ui.res.stringResource
 * import androidx.compose.ui.text.style.TextOverflow
 * import androidx.compose.ui.tooling.preview.Preview
 * import androidx.compose.ui.unit.dp
 * import androidx.lifecycle.viewmodel.compose.viewModel
 * import coil.compose.AsyncImage
 * import com.pointchange.audio.R
 * import com.pointchange.audio.model.AudioListViewModel
 * import com.pointchange.audio.model_data.AudioMetadata
 * import com.pointchange.audio.util.changeColorDeepOrLight
 * import com.pointchange.audio.view.page.PlayerDragValue
 * import kotlinx.coroutines.launch
 * import sh.calvin.reorderable.ReorderableItem
 * import sh.calvin.reorderable.rememberReorderableLazyListState
 *
 * @Composable
 * fun SimplePlayList(
 *     list: SnapshotStateList<AudioMetadata>,
 *     playListAlpha: Float,
 *     backgroundColor: Long,
 *     playerAndPlayListDraggableState: AnchoredDraggableState<PlayerDragValue>,
 *     onPlay: (uri: String) -> Unit,
 *     movePlayListItem: (from: Int, to: Int) -> Unit
 * ) {
 *     val lazyListState = rememberLazyListState()
 *     val reorderableLazyListState =
 *         rememberReorderableLazyListState(lazyListState) { from, to ->
 *             movePlayListItem(from.index, to.index)
 *         }
 *     val newBackgroundColor = changeColorDeepOrLight(
 *         rawColorInt = backgroundColor.toInt(),
 *         saturability = 0.8F,
 *         brightness = 0.3F
 *     ).toArgb()
 *         .toLong()
 *
 *     val textAndIconColor = Color.White
 *     val scope= rememberCoroutineScope()
 *     Scaffold(
 *         modifier = Modifier.padding(top = 64.dp),
 *         containerColor = Color.Transparent,
 *         floatingActionButton = {
 *             FloatingActionButton(
 *                 onClick = { scope.launch { playerAndPlayListDraggableState.animateTo(PlayerDragValue.COLLAPSED)  } },
 *             ) {
 *                 Icon(
 *                     painter = painterResource(R.drawable.round_keyboard_arrow_down_24),
 *                     contentDescription = stringResource(R.string.back),
 *                     tint = textAndIconColor
 *                 )
 *             }
 *         }
 *     ) {paddingValues ->
 *
 *     Box(
 *         modifier = Modifier.fillMaxSize()
 *             .padding(bottom = paddingValues.calculateBottomPadding())
 *             .background(color = Color(newBackgroundColor).copy(alpha = playListAlpha), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
 *     ) {
 *         LazyColumn(state = lazyListState,    modifier = Modifier.padding(bottom = 64.dp)) {
 *             items(count = list.size, key = { index -> list[index].uri }) { index ->
 *                 val item = list[index]
 *                 val title = item.title ?: item.uri
 *                 ReorderableItem(
 *                     state = reorderableLazyListState,
 *                     key = item.uri
 *                 ) { isDragging ->
 *                     Box(modifier = Modifier.longPressDraggableHandle()) {
 *                         ListItem(
 *                             modifier = Modifier.clickable(onClick = { onPlay(item.uri) }),
 *                             colors = ListItemDefaults.colors(containerColor = Color.Transparent),
 *                             leadingContent = {
 *                                 Box(modifier = Modifier.size(50.dp)) {
 *                                     if (item.coverPath != null) {
 *                                         AsyncImage(
 *                                             model = item.coverPath,
 *                                             contentDescription = null,
 *                                             modifier = Modifier
 *                                                 .clip(shape = RoundedCornerShape(10.dp))
 *                                         )
 *                                     } else {
 *                                         Box(modifier = Modifier.alpha(0.4F)){
 *                                             AudioCover(
 *                                                 mostColor = Color.White,
 *                                                 remainColor = Color(newBackgroundColor)
 *                                             )
 *                                         }
 *                                     }
 *                                 }
 *                             },
 *                             headlineContent = {
 *                                 Text(
 *                                     text = title,
 *                                     overflow = TextOverflow.Ellipsis,
 *                                     maxLines = 1,
 *                                     style = MaterialTheme.typography.titleMedium,
 *                                     color = textAndIconColor
 *                                 )
 *                             },
 *                             supportingContent = {
 *                                 if (item.artist != null) {
 *                                     Text(
 *                                         text = item.artist,
 *                                         overflow = TextOverflow.Ellipsis,
 *                                         maxLines = 1,
 *                                         style = MaterialTheme.typography.titleSmall,
 *                                         color = textAndIconColor
 *                                     )
 *                                 }
 *                             },
 *                             trailingContent = {
 *                                 Icon(
 *                                     painter = painterResource(R.drawable.round_drag_handle_24),
 *                                     contentDescription = stringResource(R.string.drag),
 *                                     modifier = Modifier.draggableHandle(),
 *                                     tint = textAndIconColor
 *                                 )
 *                             }
 *                         )
 *                     }
 *                 }
 *
 *             }
 *         }
 *     }
 *     }
 *
 * }
 *
 */



