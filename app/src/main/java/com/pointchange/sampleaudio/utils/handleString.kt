package com.pointchange.sampleaudio.utils

fun handleString(
    path: String,
    title: String?,
    artist: String?,
    album: String?,
): Pair<String, String> {
    val title =
        if (title.isNullOrEmpty())
            path.substringAfterLast("/").substringBefore(".")
        else title
    val artist = if (artist.isNullOrEmpty()) "" else artist
    val album = if (album.isNullOrEmpty()) "" else album

    val subTitle = when {
        artist.isEmpty() && album.isNotEmpty() -> album
        album.isEmpty() && artist.isNotEmpty() -> artist
        artist.isNotEmpty() && album.isNotEmpty() -> "$artist - $album"
        else -> ""
    }
    return title to subTitle
}