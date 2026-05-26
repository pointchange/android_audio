package com.pointchange.audio.util

import java.util.Locale

fun formatTime(millisecond: Long): String {
    val s = millisecond / 1000
    val second = s % 60
    val min = s / 60
    val hour = s / 3600
    return if (hour > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, min, second)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", min, second)
    }
}