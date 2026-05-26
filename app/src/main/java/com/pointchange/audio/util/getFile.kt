package com.pointchange.audio.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.text.toUpperCase
import androidx.core.content.FileProvider
import com.pointchange.audio.model_data.AudioExtension
import java.io.File
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

enum class SelectLabel {
    LRC,
    AUDIO,
    AUDIO_LRC
}

fun lrcString(data: String): String = "UPPER($data) LIKE '%.lrc'"

fun audioString(data: String): String = AudioExtension
    .entries.joinToString(" OR ") {
        "UPPER($data) LIKE '%.${it.name}'"
    }

fun getFileFromContentResolver(context: Context, selectLabel: SelectLabel): MutableList<String> {
    val uri = MediaStore.Files.getContentUri("external")

    val data = MediaStore.Files.FileColumns.DATA
    val projection = arrayOf(data)
    var selection = ""
    when (selectLabel) {
        SelectLabel.LRC -> {
            selection += lrcString(data)
        }

        SelectLabel.AUDIO -> {
            selection += audioString(data)
        }

        SelectLabel.AUDIO_LRC -> {
            selection += lrcString(data) + " OR " + audioString(data)
        }
    }

    val resList = mutableListOf<String>()
    context.contentResolver.query(uri, projection, selection, null, null)?.use {
        while (it.moveToNext()) {
            val pathIndex = it.getColumnIndex(data)
            val path = it.getString(pathIndex)
            resList.add(path)
        }
    }
    return resList
}

fun getAudioFromContentResolver(context: Context): MutableList<String> {
    val uri = MediaStore.Files.getContentUri("external")

    val data = MediaStore.Files.FileColumns.DATA
    val projection = arrayOf(data)
    val selection = AudioExtension
        .entries.joinToString(" OR ") {
            "UPPER($data) LIKE '%.${it.name}'"
        }

    val resList = mutableListOf<String>()

    context.contentResolver.query(uri, projection, selection, null, null)?.use {
        while (it.moveToNext()) {
            val pathIndex = it.getColumnIndex(data)
            val path = it.getString(pathIndex)
            resList.add(path)
        }
    }
    return resList
}

fun getAudioLrcFromContentResolver(context: Context): MutableList<String> {
    val uri = MediaStore.Files.getContentUri("external")

    val data = MediaStore.Files.FileColumns.DATA
    val projection = arrayOf(data)
    val selection = "UPPER($data) LIKE '%.lrc'"

    val resList = mutableListOf<String>()

    context.contentResolver.query(uri, projection, selection, null, null)?.use {
        while (it.moveToNext()) {
            val pathIndex = it.getColumnIndex(data)
            val path = it.getString(pathIndex)
            resList.add(path)
        }
    }
    return resList
}

fun getAudioFromFile(path: String): MutableList<String> {
    val queue: Queue<File> = LinkedList<File>()
    queue.add(File(path))
    val resList = mutableListOf<String>()

    while (!queue.isEmpty()) {
        val current = queue.poll()
        val files = current?.listFiles() ?: continue
        for (file in files) {
            if (file.isDirectory) {
                queue.add(file)
            } else {
                val isAudioFile = AudioExtension.entries.any {
                    it.toString() == file.extension.uppercase()
                }
                if (!isAudioFile) continue
                resList.add(file.absolutePath)
            }
        }
    }
    return resList
}

fun getUriFromPath(context: Context, file: File): Uri? {
    var res: Uri? = null
    val path = file.absolutePath
    val uri = MediaStore.Files.getContentUri("external")
    val id = MediaStore.Files.FileColumns._ID
    val projection = arrayOf(id)
    val data = MediaStore.Files.FileColumns.DATA + "=?"
    context.contentResolver.query(uri, projection, data, arrayOf(path), null)?.use {
        it.moveToFirst()
        val fileId = it.getLong(it.getColumnIndexOrThrow(id))
        res = ContentUris.withAppendedId(uri, fileId)
    }
    if (res == null) {
        res = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    }
    return res
}

fun getUriFromPath(context: Context, files: List<File>): MutableList<Uri> {
    val res = mutableListOf<Uri>()
    files.forEach {
        val uri = getUriFromPath(context, it)
        if (uri != null) {
            res.add(uri)
        }
    }
    return res
}

