package com.pointchange.sampleaudio.ui.component

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.pointchange.sampleaudio.R
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun LyricsItemTextDialog(
    path:String,
    onBack:()->Unit={}
) {
    val context= LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    val text = File(path)
    if (text.exists()) {
        val content = text.readText()
        AlertDialog(
            onDismissRequest = { onBack() },
            title = {
                Text(text.nameWithoutExtension)
            },
            text = {
                Text(
                    text = content,
                    modifier = Modifier.verticalScroll(state = rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    text.absolutePath, content
                                )
                            )
                        )
                    }
                    Toast.makeText(context, "复制成功", Toast.LENGTH_SHORT).show()
                    onBack()
                }) { Text(stringResource(R.string.copy)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    onBack()
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { onBack() },
            title = {
                Text(
                    text = stringResource(R.string.scan_lyrics_file_error),
                    color = MaterialTheme.colorScheme.error
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onBack()
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    onBack()
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}