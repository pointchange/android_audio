package com.pointchange.sampleaudio.media

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.pointchange.sampleaudio.data.ScanConfig
import com.pointchange.sampleaudio.data.ScanEvent
import com.pointchange.sampleaudio.data.ScanFile
import com.pointchange.sampleaudio.data.ScanType
import com.pointchange.sampleaudio.player.AudioEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.videolan.libvlc.Media
import org.videolan.libvlc.interfaces.IMedia
import java.io.File
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class Scan(private val core: AudioEngine) {
    private val resolverDispatcher = Dispatchers.IO.limitedParallelism(4)

    fun extension(path: String): String {
        return path.substringAfterLast(".", "").lowercase()
    }

    fun isAudioFile(path: String, extensions: Set<String>): Boolean {
        if (extensions.isEmpty()) return true
        return extension(path) in extensions
    }

    fun isExcludeFolder(fold: File, excludeFolders: Set<String>): Boolean {
        if (excludeFolders.isEmpty()) return false
        val path = fold.absolutePath
        return excludeFolders.any {
            val normalized = it.trimEnd(File.separatorChar)
            path == normalized || path.startsWith(normalized + File.separator)
        }
    }

    fun scan(
        context: Context,
        config: ScanConfig,
        roots: List<File> = emptyList(),
    ): Flow<ScanEvent> =
        channelFlow {
            send(ScanEvent.Start)
            currentCoroutineContext().ensureActive()
            val scannedFiles = when (config.type) {
                ScanType.ALL_FILE -> {
                    scanAllFile(roots = roots, config = config)
                }

                ScanType.MEDIA_STORE -> {
                    scanMediaStore(context = context.applicationContext, config = config)

                }
            }
            currentCoroutineContext().ensureActive()
            send(ScanEvent.Scanning(count = scannedFiles.size))
            val filterWithDuration =
                if (config.type == ScanType.ALL_FILE ) {
                    resolveDuration(files = scannedFiles) { current, total ->
                        send(ScanEvent.DurationParsing(current = current, total = total))
                    }
                } else {
                    scannedFiles
                }
            currentCoroutineContext().ensureActive()
            send(ScanEvent.Filtering)
            val result = filter(files = filterWithDuration, config = config)
            currentCoroutineContext().ensureActive()
            send(ScanEvent.Result(result))
//            send(ScanEvent.Complete)
        }.catch {
            if (it is CancellationException) {
                emit(ScanEvent.Cancelled)
            } else {
                emit(ScanEvent.Error(it))
            }
        }

    private fun filter(files: List<ScanFile>, config: ScanConfig): List<ScanFile> {
        return files.filter { file ->
            if (file.size < config.minFileSize) return@filter false
            if (config.minDurationMs > 0L && config.minDurationMs > file.durationMs) return@filter false
            true
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun resolveDuration(
        files: List<ScanFile>,
        onProcess: suspend (current: Int, total: Int) -> Unit
    ): List<ScanFile> = coroutineScope {
        if (files.isEmpty()) return@coroutineScope emptyList()
        val completed = AtomicInt(0)

        var results: List<ScanFile> = listOf()
        for (chunk in files.chunked(50)) {
            results = chunk.map { file ->
                async(resolverDispatcher) {
                    ensureActive()
                    val duration = getDuration(file.path)
                    val result = file.copy(durationMs = duration ?: 0L)
                    val current = completed.incrementAndFetch()
                    if (current % 10 == 0 || current == files.size) {
                        onProcess(current, files.size)
                    }
                    result
                }
            }.awaitAll()
        }
        results
    }

    private suspend fun scanMediaStore(context: Context, config: ScanConfig): List<ScanFile> {
        return withContext(Dispatchers.IO) {
            val result = ArrayList<ScanFile>()
            val contentResolver = context.contentResolver

            val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.DURATION,
            )

            val selection = buildString { append("${MediaStore.Audio.Media.IS_MUSIC} != 0") }
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.DATA} ASC"
            )?.use { cursor ->
                val pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val modifiedIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    ensureActive()
                    val path = cursor.getString(pathIndex)
                    if (path.isNullOrEmpty()) continue
                    if (!isAudioFile(path, config.extensions)) continue
                    val file = ScanFile(
                        id = 0,
                        path = path,
                        size = cursor.getLong(sizeIndex),
                        lastModified = cursor.getLong(modifiedIndex),
                        durationMs = cursor.getLong(durationIndex)
                    )
                    if (file.size < config.minFileSize) continue
                    if (isExcludeFolder(File(path), config.excludeFolders)) continue
                    result += file
                }
            }
            result
        }
    }

    private suspend fun scanAllFile(roots: List<File>, config: ScanConfig): List<ScanFile> {
        return withContext(Dispatchers.IO) {
            val result = ArrayList<ScanFile>()
            val stack = ArrayDeque<File>()
            roots.forEach {
                ensureActive()
                if (it.isDirectory && !isExcludeFolder(it, config.excludeFolders)) {
                    stack.addLast(it)
                }
            }
            while (stack.isNotEmpty()) {
                ensureActive()
                val direction = stack.removeLast()
                if (isExcludeFolder(direction, config.excludeFolders)) continue
                val files = direction.listFiles() ?: continue
                for (file in files) {

                    ensureActive()
                    if (file.isDirectory) {
                        stack.addLast(file)
                        continue
                    }
                    if (!file.isFile) {
                        continue
                    }
                    if (!isAudioFile(
                            path = file.absolutePath,
                            extensions = config.extensions
                        )
                    ) continue

                    val size = file.length()
                    if (size < config.minFileSize) continue
                    result += ScanFile(
                        id = 0,
                        path = file.absolutePath,
                        size = size,
                        lastModified = file.lastModified(),
                        durationMs = 0L
                    )
                }
            }
            result
        }
    }

    suspend fun getDuration(path: String): Long? {
        return withContext(Dispatchers.IO) {
            ensureActive()
            val media = Media(core.libVLC, path)
            try {
                val result = media.parse(IMedia.Parse.ParseLocal or IMedia.Parse.FetchLocal)
                if (!result) {
                    return@withContext null
                }
                val durationMs = media.duration
                if (durationMs <= 0L) {
                    null
                } else {
                    durationMs
                }
            } catch (e: Exception) {
                null
            } finally {
                media.release()
            }
        }
    }


}