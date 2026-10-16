package com.pointchange.sampleaudio.data

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "songs", indices = [Index(value = ["path"], unique = true)])
data class SongInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val coverPath: String?,
    val fileSize: Long,
    val lastModified: Long,
    @ColumnInfo(defaultValue = "0")
    val isFavorite: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val coverResolved: Boolean = false
)
//data class SongInfoMain(
//    val id: Long = 0,
//    val path: String,
//    val title: String?,
//    val artist: String?,
//    val album: String?,
//    val durationMs: Long,
//    val coverPath: String?,
//)

data class SongInfoItemLyrics(
    val id: Long = 0,
    val path: String,
)

data class SongInfoItemQueue(
    val id: Long = 0,
    val path: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
)
//data class SongInfoItemEdit(
//    val id: Long = 0,
//    val path: String,
//    val title: String?,
//    val artist: String?,
//    val album: String?,
//    val durationMs: Long,
//    val checked: Boolean=false,
//)
//fun SongInfoItemQueue.toSongInfoItemEdit():SongInfoItemEdit{
//    return SongInfoItemEdit(
//        id=id,
//        path=path,
//        title=title,
//        artist=artist,
//        album=album,
//        durationMs=durationMs,
//    )
//}

//fun SongInfo.toMediaItem(): MediaItem {
//    return MediaItem.Builder()
//        .setMediaId(path)
//        .setUri(path.toUri())
//        .setMediaMetadata(
//            MediaMetadata
//                .Builder()
//                .setTitle(title)
//                .setArtist(artist)
//                .setAlbumArtist(album)
//                .setArtworkUri(coverPath?.let(Uri::parse))
//                .build()
//        )
//        .build()
//}

val MEDIA_LIBRARY_EXTENSIONS = setOf(
    "mp3",
    "flac",
    "m4a",
    "m4b",
    "aac",
    "ogg",
    "oga",
    "opus",
    "wav",
    "aif",
    "aiff",
    "wma"
)

// 扩展格式
val FULL_SCAN_EXTENSIONS = MEDIA_LIBRARY_EXTENSIONS + setOf(
    "ape",
    "wv",
    "mpc",
    "tta",
    "ac3",
    "eac3",
    "dts",
    "mka",
    "amr",
    "mid",
    "midi"
)

data class SongScanInfo(
    val id: Long,
    val path: String,
    val fileSize: Long,
    val lastModified: Long,
)

data class SongInfoUpsert(
    val id: Long,
    val path: String,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val fileSize: Long,
    val lastModified: Long,
)


data class ResolveMedia(
    val id: Long,
    val file: ScanFile,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
)


data class GetSongCoverInfo(
    val id: Long = 0,
    val path: String,
)

data class SetSongCoverInfo(
    val path: String,
    val coverPath: String?,
)

data class AudioInfo(
    // ─────────────
    // File
    // ─────────────
    val path: String?,
    val fileName: String?,
    val fileSize: Long?,

    // ─────────────
    // Media
    // ─────────────
    val durationMs: Long?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val genre: String?,
    val copyright: String?,
    val publisher: String?,
    val description: String?,
    val language: String?,
    val date: String?,

    // ─────────────
    // Audio
    // ─────────────
    val codec: String?,
    val codecDescription: String?,
    val sampleRate: Int?,
    val channels: Int?,
    val bitrate: Long?,
    val bitsPerSample: Int?,

    // ─────────────
    // Track
    // ─────────────
    val trackNumber: Int?,
    val discNumber: Int?,

    // ─────────────
    // Artwork
    // ─────────────
    val artworkUri: String?,
)

data class InfoItem(
    val label: String,
    val value: String?
)

fun AudioInfo.toInfoItems(): List<InfoItem> {
    return listOf(
        // ─────────────
        // File
        // ─────────────
        InfoItem("路径", path),
        InfoItem("文件名", fileName),
        InfoItem("文件大小", fileSize?.formatFileSize()),

        // ─────────────
        // Media
        // ─────────────
        InfoItem("时长", durationMs?.formatDuration()),
        InfoItem("标题", title),
        InfoItem("艺术家", artist),
        InfoItem("专辑", album),
        InfoItem("流派", genre),
        InfoItem("版权", copyright),
        InfoItem("出版商", publisher),
        InfoItem("描述", description),
        InfoItem("语言", language),
        InfoItem("日期", date),

        // ─────────────
        // Audio
        // ─────────────
        InfoItem("编码", codec),
        InfoItem("编码描述", codecDescription),
        InfoItem(
            "采样率",
            sampleRate?.let { "$it Hz" }
        ),
        InfoItem(
            "声道",
            channels?.let { channelCount ->
                when (channelCount) {
                    1 -> "Mono (1)"
                    2 -> "Stereo (2)"
                    else -> "$channelCount channels"
                }
            }
        ),
        InfoItem(
            "比特率",
            bitrate?.formatBitrate()
        ),
        InfoItem(
            "位深",
            bitsPerSample?.let { "$it bit" }
        ),

        // ─────────────
        // Track
        // ─────────────
        InfoItem(
            "音轨",
            trackNumber?.toString()
        ),
        InfoItem(
            "碟片",
            discNumber?.toString()
        ),

        // ─────────────
        // Artwork
        // ─────────────
//        InfoItem(
//            "封面",
//            artworkUri
//        )
    )
        .filter { !it.value.isNullOrBlank() }
}

private fun Long.formatFileSize(): String {
    if (this < 1024) {
        return " $this B"
    }

    val kb = this / 1024.0

    if (kb < 1024) {
        return " %.2f KB".format(kb)
    }

    val mb = kb / 1024.0

    if (mb < 1024) {
        return " %.2f MB".format(mb)
    }

    val gb = mb / 1024.0

    return " %.2f GB".format(gb)
}

private fun Long.formatBitrate(): String {
    return when {
        this >= 1_000_000 -> {
            "%.2f Mbps".format(this / 1_000_000.0)
        }

        this >= 1_000 -> {
            "${this / 1_000} kbps"
        }

        else -> {
            "$this bps"
        }
    }
}

private fun Long.formatDuration(): String {
    val totalSeconds = this / 1000

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        "%d:%02d:%02d".format(
            hours,
            minutes,
            seconds
        )
    } else {
        "%02d:%02d".format(
            minutes,
            seconds
        )
    }
}


