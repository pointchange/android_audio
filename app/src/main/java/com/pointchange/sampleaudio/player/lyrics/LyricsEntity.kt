package com.pointchange.sampleaudio.player.lyrics

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pointchange.sampleaudio.data.SongInfo

@Entity(
    tableName = "lyrics",
    foreignKeys = [
        ForeignKey(
            entity = SongInfo::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["songId"], unique = true)]
)
data class LyricsEntity(
    @PrimaryKey
    val songId: Long,
    val path: String,
    val format: String,
    val lastModified: Long,
)

data class LyricsLine(
    val index: Int,
    val startTimeMs: Long,
    val endTimeMs: Long?,
    val text: String,
)

data class LyricsDocument(
    val line: List<LyricsLine>,
    val timed: Boolean = true,
    val path: String = "",
    val id: Long = 0L
)
