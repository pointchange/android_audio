package com.pointchange.audio.view

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

//
//enum class NavID {
//    HOME,
//    PLAYER,
//    FAVORITE,
//    LIST_OPERATION,
//    INFORMATION,
//    SCAN,
//    SCAN_LRC,
//    SETTING,
//    TIMER,
//    THEME,
//}

@Serializable
sealed interface NavID : NavKey {
    //(val playerPage: @Composable () -> Unit)
    @Serializable
    data object Home : NavID
    @Serializable
    data object Player : NavID
    @Serializable
    data object Favorite : NavID

    @Serializable
    data object ListOperation : NavID

    @Serializable
    data object Information : NavID

    @Serializable
    data object Scan : NavID

    @Serializable
    data object ScanLrc : NavID

    @Serializable
    data object Setting : NavID

    @Serializable
    data object Theme : NavID


    @Serializable
    data object Timer : NavID
@Serializable
data object AudioEffect: NavID

}