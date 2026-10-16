package com.pointchange.sampleaudio

import android.app.Application
import android.util.Log
import com.pointchange.sampleaudio.container.DatabaseContainer
import com.pointchange.sampleaudio.player.AudioEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class App : Application() {
    val core by lazy { AudioEngine(this) }

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val databaseContainer: DatabaseContainer by lazy { DatabaseContainer(this) }
    override fun onCreate() {
        super.onCreate()
        scope.launch {
            val songInfos = databaseContainer.dataStore.songInfoMains.first()
            songInfos?.let{
                databaseContainer.libraryQueue.setFirstLoadingData(songInfos)
            }
            scope.cancel()
        }
    }

}