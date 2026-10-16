package com.pointchange.sampleaudio.media

import android.util.Log
import com.pointchange.sampleaudio.data.AudioInfo
import com.pointchange.sampleaudio.data.ResolveMedia
import com.pointchange.sampleaudio.data.ScanFile
import com.pointchange.sampleaudio.data.GetSongCoverInfo
import com.pointchange.sampleaudio.data.SetSongCoverInfo
import com.pointchange.sampleaudio.player.AudioEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.videolan.libvlc.Media
import org.videolan.libvlc.interfaces.IMedia
import java.io.File
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class, ExperimentalAtomicApi::class)
class MediaResolver(
    private val core: AudioEngine,
    private val artworkResolver: ArtworkResolver,
    private val coverStorage: CoverStorage
) {
    private val resolverDispatcher = Dispatchers.IO.limitedParallelism(4)
    suspend fun <T> resolveSongs(
        files: List<ScanFile>,
        transform: suspend (ResolveMedia) -> T?,
        onProcess: suspend (current: Int, total: Int) -> Unit,
        onBatch: suspend (List<T>) -> Unit
    ) = coroutineScope {
        if (files.isEmpty()) return@coroutineScope
        val total = files.size
        val completed = AtomicInt(0)
        for (chunk in files.chunked(50)) {
            ensureActive()
            val results = chunk
                .map { file ->
                    async(resolverDispatcher) {
                        ensureActive()
                        val resolved = resolveMedia(file)
                        val result = resolved?.let { transform(it) }
                        val current = completed.incrementAndFetch()
                        if (current % 10 == 0 || current == total) {
                            onProcess(current, total)
                        }
                        result
                    }
                }.awaitAll()
            val batch = results.filterNotNull()
            if (batch.isNotEmpty()) onBatch(batch)
        }
    }

    suspend fun resolveMedia(file: ScanFile): ResolveMedia? = withContext(Dispatchers.IO) {
        val media = Media(core.libVLC, file.path)
        try {
            val result = media.parse(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal)
            if (!result) {
                return@withContext null
            }
            val durationMs = media.duration
            if (durationMs <= 0L) return@withContext null
            val title = media.getMeta(IMedia.Meta.Title)
            val artist = media.getMeta(IMedia.Meta.Artist)
            val album = media.getMeta(IMedia.Meta.Album)
            ResolveMedia(
                id = file.id,
                file = file,
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
            )
        } catch (e: Exception) {
            null
        } finally {
            media.release()
        }
    }

    suspend fun resolveCovers(
        onGetCoverList: suspend () -> List<GetSongCoverInfo>,
        onUpdate: suspend (setSongCoverInfo: SetSongCoverInfo) -> Unit
    ) = coroutineScope {
        while (true) {
            ensureActive()
            val songs = onGetCoverList()
            if (songs.isEmpty()) break
            songs.map { song -> async(resolverDispatcher) { resolveCover(song, onUpdate) } }
                .awaitAll()
        }
    }

    private suspend fun resolveCover(
        song: GetSongCoverInfo,
        onUpdate: suspend (setSongCoverInfo: SetSongCoverInfo) -> Unit
    ): Boolean {
        try {
            val file = artworkResolver.resolve(song.path)
            if (file == null) {
                onUpdate(SetSongCoverInfo(path = song.path, coverPath = null))
                return false
            }
            val coverPath = coverStorage.save(source = file)
            onUpdate(SetSongCoverInfo(path = song.path, coverPath = coverPath))
            return coverPath != null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return false
        }
    }


}