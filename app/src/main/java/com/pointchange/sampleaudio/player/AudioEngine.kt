package com.pointchange.sampleaudio.player

import android.content.Context
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.MediaPlayer

class AudioEngine(context: Context) {
    val args = arrayListOf(
        "--no-video",
        "--audio-time-stretch",
    )
    val libVLC = LibVLC(context.applicationContext, args)
    val vlcPlayer = MediaPlayer(libVLC)
    var volume: Int = 100
        private set

    fun setUserVolume(userVolume: Int) {
        volume = userVolume
        vlcPlayer.volume = userVolume
    }

    fun release() {
        libVLC.release()
    }
}