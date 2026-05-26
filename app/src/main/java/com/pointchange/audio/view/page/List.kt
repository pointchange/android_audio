package com.pointchange.audio.view.page

import android.content.Context
import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class List(@param:StringRes val label: Int) {
    FAVORITE(R.string.favorite);

    fun getString(context: Context): String {
        return context.getString(label)
    }
}