package com.pointchange.audio.view

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.pointchange.audio.view.NavID.Favorite
import com.pointchange.audio.view.NavID.Home
import com.pointchange.audio.view.NavID.Information
import com.pointchange.audio.view.NavID.ListOperation
import com.pointchange.audio.view.NavID.Scan
import com.pointchange.audio.view.NavID.ScanLrc
import com.pointchange.audio.view.NavID.Setting
import com.pointchange.audio.view.NavID.Theme
import com.pointchange.audio.view.NavID.Timer
import com.pointchange.audio.view.page.AudioEffectPage
import com.pointchange.audio.view.page.FavoritePage
import com.pointchange.audio.view.page.HomePage
import com.pointchange.audio.view.page.InformationPage
import com.pointchange.audio.view.page.ListOperationPage
import com.pointchange.audio.view.page.PlayerPage
import com.pointchange.audio.view.page.ScanLrcPage
import com.pointchange.audio.view.page.ScanPage
import com.pointchange.audio.view.page.SettingPage
import com.pointchange.audio.view.page.ThemePage
import com.pointchange.audio.view.page.TimerPage

val LocalNav = staticCompositionLocalOf<NavActions?> { null }

@Composable
fun MainNav() {
//    val backStack = rememberSaveable { mutableStateListOf<NavID>(Home) }

//    val list: Array<out NavKey> = arrayOf(Home.Companion)
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(elements = arrayOf(Home))
    val actions = remember(backStack) {
        object : NavActions {
            override fun add(id: NavID) {
                backStack.add(id)
            }

            override fun removeLast() {
                backStack.removeLastOrNull()
            }

            override fun isLastOrNull() = backStack.lastOrNull()

        }
    }
//    val entryProvider: (NavKey) -> NavEntry<NavKey> = entryProvider {
//        entry<Home> {
//            HomePage(
//                sharedTransitionScope = this@,
//                animatedVisibilityScope=this@entryProvider
//            )
//        }
//        entry<Player> { PlayerPage() }
//        entry<Favorite> {
//            FavoritePage()
//        }
//        entry<ListOperation> { ListOperationPage() }
//        entry<Information> { InformationPage() }
//        entry<Scan> { ScanPage() }
//        entry<ScanLrc> { ScanLrcPage() }
//        entry<Setting> { SettingPage() }
//        entry<Theme> { ThemePage() }
//        entry<Timer> { TimerPage() }
//    }
    CompositionLocalProvider(LocalNav provides actions) {

        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
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
            entryProvider = entryProvider {
                entry<Home> { HomePage() }
                entry<NavID.Player> { PlayerPage() }
                entry<Favorite> { FavoritePage() }
                entry<ListOperation> { ListOperationPage() }
                entry<Information> { InformationPage() }
                entry<Scan> { ScanPage() }
                entry<ScanLrc> { ScanLrcPage() }
                entry<Setting> { SettingPage() }
                entry<Theme> { ThemePage() }
                entry<Timer> { TimerPage() }
                entry<NavID.AudioEffect> { AudioEffectPage() }
            }
        )
    }
}

/**
 *
 * { key ->
 *             when (key) {
 *                 is Home -> NavEntry(key) { HomePage() }
 *                 is Player -> NavEntry(key) { PlayerPage() }
 *                 is Favorite -> NavEntry(key) { FavoritePage() }
 *                 is ListOperation -> NavEntry(key) { ListOperationPage() }
 *                 is Information -> NavEntry(key) { InformationPage() }
 *                 is Scan -> NavEntry(key) { ScanPage() }
 *                 is ScanLrc -> NavEntry(key) { ScanLrcPage() }
 *                 is Setting -> NavEntry(key) { SettingPage() }
 *                 is Theme -> NavEntry(key) { ThemePage() }
 *                 is Timer -> NavEntry(key) { TimerPage() }
 *             }
 *         }
 *
 *
 */