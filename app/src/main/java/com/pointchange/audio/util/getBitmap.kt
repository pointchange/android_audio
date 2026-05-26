package com.pointchange.audio.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import java.io.File

fun getBitmap(cover: String): Bitmap? {
    return try {
        if (cover == "") {
            null
        } else {
            val file = File(cover.toUri().path ?: "")
            if (file.exists()) {
                BitmapFactory.decodeFile(file.path)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}

fun changeColorDeepOrLight(rawColorInt:Int,saturability: Float,brightness: Float): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(rawColorInt, hsl)
    hsl[1] = hsl[1].coerceAtMost(saturability)
    hsl[2] = brightness
    return  Color(ColorUtils.HSLToColor(hsl))
}