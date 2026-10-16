package com.pointchange.sampleaudio.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pointchange.sampleaudio.database.SongRepository
import com.pointchange.sampleaudio.media.PlayerConnection
import com.pointchange.sampleaudio.player.queue.LibraryQueue
//    private val playerConnection by lazy {
//        PlayerConnection(this)
//    }
//    private val viewModel: PlayerStateViewModel by viewModels {
//        PlayerViewModelFactory(
//            playerConnection = playerConnection,
//            songRepository = (application as App).databaseContainer.songRepository,
//            libraryQueue = (application as App).databaseContainer.libraryQueue
//        )
//    }
//class PlayerViewModelFactory(
//    private val playerConnection: PlayerConnection,
//    private val songRepository: SongRepository,
//    private val libraryQueue: LibraryQueue,
//) :
//    ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(PlayerStateViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return PlayerStateViewModel(
//                connection = playerConnection,
//                songRepository = songRepository,
//                libraryQueue=libraryQueue,
//            ) as T
//        }
//
//        throw IllegalArgumentException("Unknow ViewModel class ${modelClass.name}")
//    }
//}