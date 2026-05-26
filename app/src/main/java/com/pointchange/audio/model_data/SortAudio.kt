package com.pointchange.audio.model_data

import android.content.Context
import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class SortAudio(@param:StringRes val label: Int) {
    DEFAULT(R.string.default_sort) {
        override fun description() = "default"
    },
    TITLE(R.string.title) {
        override fun description() = "title"
    },
    ARTIST(R.string.artist) {
        override fun description() = "artist"
    };

    fun getString(context: Context): String {
        return context.applicationContext.getString(label)
    }

    abstract fun description(): String
}