package com.pointchange.audio.model

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointchange.audio.model_data.AudioMetadata
import com.pointchange.audio.model_data.dataStore
import com.pointchange.audio.service.VlcManager
import com.pointchange.audio.util.getAudioLrcFromContentResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import java.io.File

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    val isPlaying = VlcManager.isPlaying
    val duration = VlcManager.duration
    val current = VlcManager.current
    val playError = VlcManager.playError
    private val _multiplePlayBack = MutableStateFlow(1.0f)
    val multiplePlayBack = _multiplePlayBack.asStateFlow()

    init {
        viewModelScope.launch {
            playError.collect {
                if (it) {
                    Toast.makeText(application, "播放出错", Toast.LENGTH_SHORT).show()
                    VlcManager.setPlayError()
                }
            }
        }
    }

    fun setMultiplePlayBack(velocity: Float) {
        _multiplePlayBack.value = velocity
        VlcManager.mediaPlayer?.rate = velocity
    }

    fun play() {
        VlcManager.mediaPlayer?.play()
    }

    fun pause() {
        VlcManager.mediaPlayer?.pause()
    }

    fun seekTo(newCurrent: Float) {
        val current = newCurrent.toLong()
        VlcManager.seekTo(current)
    }

    fun getVolume(): Int {
        val volume = VlcManager.getVolume()
        return volume ?: 100
    }

    fun setVolume(volume: Int) {
        VlcManager.setVolume(volume)
    }

}