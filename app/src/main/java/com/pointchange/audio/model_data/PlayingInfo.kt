package com.pointchange.audio.model_data

import com.pointchange.audio.model.PlayList
import kotlinx.serialization.Serializable

@Serializable
data class PlayingInfo(
    var index:Int=0,
    var currentList: PlayList= PlayList.DEFAULT,
    var mode: PlayMode= PlayMode.LOOP,
    var audioMetadata: AudioMetadata?=null
)

