package com.pointchange.audio.view.page

import android.content.Context
import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class NavigationDrawer(@param:StringRes val label: Int) {
    SCAN(R.string.scan),
    SETTINGS(R.string.settings);

    fun getString(context: Context): String {
        return context.getString(label)
    }
}