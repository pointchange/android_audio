package com.pointchange.sampleaudio.data

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

data class PlayerUiState(
    val isPlaying: Boolean = false,
//    val currentMediaItem: MediaItem? = null,
    val durationMs: Long = 0L,
//    val positionMs: Long = 0L,
//    val playbackState: Int = Player.STATE_IDLE
    val error: String?=null,
)
