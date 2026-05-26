package com.pointchange.audio.view.widget

import kotlinx.serialization.Serializable

@Serializable
data class AudioInfoWidget(
    val title: String = "",
    val artist: String = "",
    val cover: String = "",
    val isPlaying: Boolean = false
)