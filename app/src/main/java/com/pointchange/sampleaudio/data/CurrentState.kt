package com.pointchange.sampleaudio.data

import android.net.Uri
import android.net.Uri.parse
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.pointchange.sampleaudio.player.queue.PlayerMode
import kotlinx.serialization.Serializable

@Serializable
data class CurrentState(
    val id: Long = 0,
    val path: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0L,
    val coverPath: String? = null,
    val isFavorite: Boolean = false,
    val index: Int = -1,
    val speed: Float = 1.0f,
    val positionMs: Long = 0L,
    val mode: PlayerMode = PlayerMode.DEFAULT,
)

fun CurrentState.toMediaItem(): MediaItem? {
    return path?.let {
        MediaItem.Builder()
            .setMediaId(it)
            .setUri(it.toUri())
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumArtist(album)
                    .setArtworkUri(coverPath?.let(Uri::parse))
                    .build()
            )
            .build()
    }
}