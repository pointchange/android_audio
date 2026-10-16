package com.pointchange.sampleaudio.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pointchange.sampleaudio.data.SongInfo
import com.pointchange.sampleaudio.player.lyrics.LyricsDao
import com.pointchange.sampleaudio.player.lyrics.LyricsEntity
import com.pointchange.sampleaudio.player.queue.QueueDao
import com.pointchange.sampleaudio.player.queue.QueueInfo

@Database(entities = [SongInfo::class, QueueInfo::class, LyricsEntity::class], version = 1, exportSchema = true)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun queueDao(): QueueDao

    abstract fun lyricsDao(): LyricsDao

}