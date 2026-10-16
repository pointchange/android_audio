package com.pointchange.sampleaudio.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import androidx.core.graphics.scale

class CoverStorage(context: Context) {
    private val coverDir = File(context.filesDir, "covers").apply { mkdirs() }
    private val MAX_SIZE = 1000
    private val JPEG_QUALITY = 90
    suspend fun save(source: File): String? = withContext(Dispatchers.IO) {
        val target = File(coverDir, "${sha256(source.absolutePath)}.jpg")
        val temp = File(coverDir, "${target.name}.jpg")
        try {
            val bitmap = decodeAndScale(source) ?: return@withContext null
            try {
                temp.outputStream().use { output ->
                    val success = bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        JPEG_QUALITY,
                        output,
                    )

                    if (!success) {
                        return@withContext null
                    }

                    output.flush()
                }

                if (!temp.renameTo(target)) {
                    temp.copyTo(target, overwrite = true)
                    temp.delete()
                }

                target.absolutePath

            } finally {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            temp.delete()
            null
        }
//        source.inputStream().use { inputStream ->
//            target.outputStream().use { outputStream ->
//                inputStream.copyTo(outputStream)
//            }
//        }
//        target.absolutePath
    }

    private fun decodeAndScale(file: File): Bitmap? {

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        val width = bounds.outWidth
        val height = bounds.outHeight

        if (width <= 0 || height <= 0) {
            return null
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSampleSize(
                width = width,
                height = height,
                maxSize = MAX_SIZE,
            )
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
        return scaleToMaxSize(bitmap, MAX_SIZE)
    }

    private fun calculateSampleSize(width: Int, height: Int, maxSize: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= maxSize && height / (sample * 2) >= maxSize) {
            sample *= 2
        }
        return sample
    }

    private fun scaleToMaxSize(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val scale = maxSize.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        val scaled = bitmap.scale(newWidth, newHeight)
        if (scaled !== bitmap) {
            bitmap.recycle()
        }

        return scaled
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(value.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    suspend fun delete(coverPath: String?) = withContext(Dispatchers.IO) {
        if (coverPath.isNullOrBlank()) return@withContext
        runCatching { File(coverPath).delete() }
    }
}