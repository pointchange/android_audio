package com.pointchange.audio.view.component

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.pointchange.audio.util.getUriFromPath
import java.io.File

@Composable
fun ShareOneAudioFile(uri:String,context: Context,shareTitle:String="") {
    val file = File(uri)
    val uri =
        getUriFromPath(context, file) ?: return
    Intent(Intent.ACTION_SEND).apply {
        type = "*/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri(file.nameWithoutExtension, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(this, shareTitle))
    }
}