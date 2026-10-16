package com.pointchange.sampleaudio.container

import android.content.Context
import androidx.room.Room
import com.pointchange.sampleaudio.App
import com.pointchange.sampleaudio.database.SongDatabase
import com.pointchange.sampleaudio.database.SongRepository
import com.pointchange.sampleaudio.datastore.DataStoreProvider
import com.pointchange.sampleaudio.player.Equalizer
import com.pointchange.sampleaudio.player.lyrics.LyricsRepository
import com.pointchange.sampleaudio.player.queue.LibraryQueue
import com.pointchange.sampleaudio.player.queue.QueueRepository

class DatabaseContainer(context: Context) {
    val database =
        Room.databaseBuilder(context.applicationContext, SongDatabase::class.java, "songs.db")
            .build()
    val songRepository = SongRepository(database.songDao())
    val queueDao = database.queueDao()
    val queueRepository = QueueRepository(queueDao)
    var libraryQueue = LibraryQueue(songRepository, queueRepository)
    val lyricsRepository = LyricsRepository(database.lyricsDao())
    val dataStore = DataStoreProvider.get(context)
    val equalizer = Equalizer((context as App).core)
}