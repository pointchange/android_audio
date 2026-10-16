package com.pointchange.sampleaudio.media

import androidx.core.net.toUri
import com.pointchange.sampleaudio.player.AudioEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.videolan.libvlc.Media
import org.videolan.libvlc.interfaces.IMedia
import java.io.File

class ArtworkResolver(private val core: AudioEngine) {
    suspend fun resolve(path: String): File? = withContext(Dispatchers.IO) {
        val media = Media(core.libVLC, path)
        try {
            val result = media.parse(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal)
            if (!result) {
                return@withContext null
            }
            val url = media.getMeta(IMedia.Meta.ArtworkURL)
            if (url.isNullOrBlank()) return@withContext null
            resolveFile(url)

        } catch (e: Exception) {
            null
        } finally {
            media.release()
        }
    }

    private fun resolveFile(url: String): File?{
        val uri = url.toUri()
        if(uri.scheme!="file"){
            return null
        }
        val path = uri.path ?: return null
        return File(path).takeIf { it.isFile }

    }

}