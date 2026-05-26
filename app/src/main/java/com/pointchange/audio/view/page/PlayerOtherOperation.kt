package com.pointchange.audio.view.page

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class PlayerOtherOperation(@param:StringRes val text:Int,@param:DrawableRes val icon:Int) {
    SHARE(icon = R.drawable.round_share_24, text = R.string.share_local_files),
    INFO(icon = R.drawable.outline_info_24, text = R.string.audio_info),
    MULTIPLE_PLAYBACK(icon = R.drawable.round_1x_mobiledata_24, text = R.string.multiple_playback),
}