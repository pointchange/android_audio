package com.pointchange.audio.model_data

import android.content.Context
import androidx.annotation.StringRes
import com.pointchange.audio.R

enum class PlayMode(@param:StringRes val label: Int) {
    LOOP(R.string.loop),
    REPEAT(R.string.repeat_one),
    RANDOM(R.string.random);

    fun getString(context: Context): String =
        context.getString(label)

}