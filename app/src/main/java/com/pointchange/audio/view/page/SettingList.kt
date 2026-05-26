package com.pointchange.audio.view.page

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.pointchange.audio.R
import com.pointchange.audio.view.NavID

enum class SettingList(@param:StringRes val text: Int, @param:DrawableRes val icon: Int,val nav: NavID) {
    THEME(text = R.string.setting_theme, icon = R.drawable.round_color_lens_24, nav = NavID.Theme),
    TIMER(text = R.string.setting_timer, icon = R.drawable.round_av_timer_24, nav = NavID.Timer),
}

enum class SettingListNotNav(@param:StringRes val text: Int, @param:DrawableRes val icon: Int) {
    PARSE(text = R.string.parse_file, icon = R.drawable.round_sync_24),
}