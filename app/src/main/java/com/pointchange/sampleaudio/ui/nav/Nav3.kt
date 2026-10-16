package com.pointchange.sampleaudio.ui.nav

import android.widget.Toast
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.pointchange.sampleaudio.data.Route
import com.pointchange.sampleaudio.data.Route.*
import com.pointchange.sampleaudio.ui.page.EditListPage
import com.pointchange.sampleaudio.ui.page.FolderFilterPage
import com.pointchange.sampleaudio.ui.page.Home
import com.pointchange.sampleaudio.ui.page.Information
import com.pointchange.sampleaudio.ui.page.LyricsInformationPage
import com.pointchange.sampleaudio.ui.page.LyricsPage
import com.pointchange.sampleaudio.ui.page.ManageListPage
import com.pointchange.sampleaudio.ui.page.PlayerPage
import com.pointchange.sampleaudio.ui.page.ScanLrcFile
import com.pointchange.sampleaudio.ui.page.ScanPage
import com.pointchange.sampleaudio.ui.page.TimerPage
import com.pointchange.sampleaudio.viewmodel.Nav3ViewModel
import com.pointchange.sampleaudio.viewmodel.PlayerStateViewModel
import kotlinx.serialization.Serializable


@Composable
fun Nav3() {
    val context = LocalContext.current
    val navigator = viewModel<Nav3ViewModel>()

    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.pop() },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
    ) { route ->
        when (route) {
            Route.Home -> {
                NavEntry(route) {
                    Home()
                }
            }

            is Info -> {
                NavEntry(route) {
                    if (route.path.isNotEmpty()) {
                        Information(route.path)
                    } else {
                        Toast.makeText(context, "无效信息", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            is EditList -> {
                NavEntry(route) {
                    EditListPage(route.queueName, route.queueList)
                }
            }

            is ManageList -> {
                NavEntry(route) {
                    ManageListPage()
                }
            }

            Player -> {
                NavEntry(route) {
                    PlayerPage()
                }
            }

            Lyrics -> {
                NavEntry(route) {
                    LyricsPage()
                }
            }

            Timer -> {
                NavEntry(route) {
                    TimerPage()
                }
            }

            Scan -> {
                NavEntry(route) {
                    ScanPage()
                }
            }

            FolderFilter -> {
                NavEntry(route) {
                    FolderFilterPage()
                }
            }

            ScanLrc -> {
                NavEntry(route) {
                    ScanLrcFile()
                }
            }

            LyricsInformation ->  {
                NavEntry(route) {
                    LyricsInformationPage()
                }
            }
        }
    }

}