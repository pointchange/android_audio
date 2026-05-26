package com.pointchange.audio.model_data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.serialization.Serializable

@Database(entities = [AudioMetadata::class], version = 1, exportSchema = false)
abstract class AudioDatabase : RoomDatabase() {
    abstract fun audioMetadataDao(): AudioMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AudioDatabase? = null
        fun getDatabase(context: Context): AudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioDatabase::class.java,
                    "audio_db"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}