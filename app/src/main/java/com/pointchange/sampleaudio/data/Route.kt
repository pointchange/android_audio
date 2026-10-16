package com.pointchange.sampleaudio.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data class Info(val path: String) : Route

    @Serializable
    data class EditList(
        val queueName: String,
        val queueList: List<Long>,
    ) : Route

    @Serializable
    data object ManageList : Route
    @Serializable
    data object Player : Route
    @Serializable
    data object Lyrics: Route
    @Serializable
    data object Timer: Route
    @Serializable
    data object Scan: Route
    @Serializable
    data object FolderFilter: Route
    @Serializable
    data object ScanLrc: Route
    @Serializable
    data object LyricsInformation: Route
}
