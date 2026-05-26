package com.pointchange.audio.view.page

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class ListOperation(@param:DrawableRes val icon: Int, @param:StringRes val text: Int) {
    DELETE(icon = R.drawable.round_delete_outline_24, text = R.string.delete),
    ADD_TO_THE_PLAY_LIST(
        icon = R.drawable.round_playlist_add_24,
        text = R.string.add_to_the_playlist
    ),
    PLAY_THE_NEXT_ONE(
        icon = R.drawable.round_queue_play_next_24,
        text = R.string.play_the_next_one
    ),
    SHARE_LOCAL_FILES(icon = R.drawable.round_share_24, text = R.string.share_local_files);


}