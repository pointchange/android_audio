package com.pointchange.audio.model_data

import androidx.room.PrimaryKey

data class AudioItem(
    val uri: String,
    val artist: String?,
    val title: String?,
    val album:String? = null,
    val coverPath: String? = null,
)