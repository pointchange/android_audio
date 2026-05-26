package com.pointchange.audio.view.page

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import com.pointchange.audio.view.component.AudioCover
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun Test100() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val size = 400F
    //                    mostColor = Color(0xFF444444),
    //                        remainColor = Color(0xFF222222)
    val paint = android.graphics.Paint().apply {
//        color = android.graphics.Color.GRAY
        color = 0xFF444444.toInt()

    }
    val paintFew = android.graphics.Paint().apply {
        color = 0xFF222222.toInt()

    }
    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    val uuid = Uuid.random()
                    val bitmap = createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                    val fileName = "light_cover$uuid.jpg"
                    val file = File(context.getExternalFilesDir(null), fileName)
                    android.graphics.Canvas(bitmap).also {
                        it.drawRect(0F, 0F, size, size, paint)
                        it.drawCircle(200F, 200F, 200F * 0.8F, paintFew)
                        it.drawCircle(200F, 200F, 200F * 0.25F, paint)
                        it.translate(400F / 1.8F, 0F)
                        it.drawRect(0F, 0F, size / 14F, size / 2F, paint)
                    }

                    try {
                        FileOutputStream(file).use {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)

                        }
                        Log.d("str123", file.absolutePath)
                    } catch (e: Exception) {
                        Log.d("str123", "${e}")
                    }
                }
            }) {
                Text(text = "保存")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
        }

    }
}

@Preview(showBackground = true)
@Composable
fun Test100Preview() {
    Test100()
}