package com.pointchange.audio.view.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class AnAudioOperation(@param:DrawableRes val icon: Int, @param:StringRes val text: Int) {
    PLAY_THE_NEXT_ONE(
        icon = R.drawable.round_queue_play_next_24,
        text = R.string.play_the_next_one
    ),
    ADD_FAVORITE(icon = R.drawable.round_favorite_24, text = R.string.favorite),
    AUDIO_INFO(icon = R.drawable.outline_info_24, text = R.string.audio_info),
    DELETE(icon = R.drawable.round_delete_outline_24,text=R.string.delete),
    SHARE_LOCAL_FILES(icon = R.drawable.round_share_24, text = R.string.share_local_files)
}