package com.pointchange.audio.model_data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "audio_metadata")
data class AudioMetadata(
    @PrimaryKey val uri: String,
    val artist:String? = null,
    val album:String? = null,
    val title:String? = null,
    val coverPath: String? = null,
    val isFavorite: Boolean = false,
    val lrc:String? = null
)