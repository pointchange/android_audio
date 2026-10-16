package com.pointchange.sampleaudio.player

import org.videolan.libvlc.MediaPlayer

class Equalizer(private val core: AudioEngine) {
    val mediaPlayer = core.vlcPlayer
    private var equalizer: MediaPlayer.Equalizer = MediaPlayer.Equalizer.create()

    init {
        mediaPlayer.setEqualizer(equalizer)
    }

    fun getPresetNames(): List<String> {
        val count = MediaPlayer.Equalizer.getPresetCount()
        return (0 until count).map { MediaPlayer.Equalizer.getPresetName(it) }
    }

    fun setPreset(presetIndex: Int) {
        // 3.7.0 直接通过静态工厂方法从预设创建新实例
        val vlcEqualizer = MediaPlayer.Equalizer.createFromPreset(presetIndex)
        // 必须应用到当前播放器才会即时生效
        mediaPlayer.setEqualizer(vlcEqualizer)
    }

    fun setPreamp(value: Float) {
        equalizer.preAmp = value
        mediaPlayer.setEqualizer(equalizer)
    }

    fun setBand(index: Int, value: Float) {
        equalizer.setAmp(index, value)
        mediaPlayer.setEqualizer(equalizer)
    }

    fun getBand(index: Int): Float {
        return equalizer.getAmp(index)
    }

    fun reset() {
        equalizer = MediaPlayer.Equalizer.create()
        mediaPlayer.setEqualizer(equalizer)
    }

    fun disable() {
        mediaPlayer.setEqualizer(null)
    }

    fun enable() {
        mediaPlayer.setEqualizer(equalizer)
    }
}