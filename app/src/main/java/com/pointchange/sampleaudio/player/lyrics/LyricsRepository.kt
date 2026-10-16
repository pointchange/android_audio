package com.pointchange.sampleaudio.player.lyrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File


class LyricsRepository(val lyricsDao: LyricsDao) {
    private val lrcTimeRegex = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")
    private val srtTimeRegex =
        Regex("""(\d{2}):(\d{2}):(\d{2})[,.](\d{3})\s*-->\s*(\d{2}):(\d{2}):(\d{2})[,.](\d{3})""")
    val format = setOf(
        "lrc",
        "srt"
    )

    suspend fun scan(roots: List<File>, paths: List<String>): List<String> =
        withContext(Dispatchers.IO) {
            if (paths.isEmpty()) return@withContext emptyList()
            val stack = ArrayDeque<File>()
            val result = mutableListOf<String>()
            val names = paths.map { it.substringAfterLast("/").trim() }
            roots.forEach {
                ensureActive()
                if (it.isDirectory) {
                    stack.addLast(it)
                }
            }
            while (stack.isNotEmpty()) {
                ensureActive()
                val direction = stack.removeLast()
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
                    if (file.nameWithoutExtension.trim() !in names) continue
                    if (file.extension.lowercase() !in format) continue
                    result.add(file.absolutePath)
                }
            }
            result
        }

    suspend fun getLyricsBySongId(songId: Long): LyricsEntity? = lyricsDao.getBySongId(songId)
    suspend fun loadLyrics(songId: Long): LyricsDocument? = withContext(Dispatchers.IO) {
        val lyricsEntity = getLyricsBySongId(songId)
        val path = lyricsEntity?.path ?: return@withContext null
        val id = lyricsEntity.songId
        val file = File(path)
        if (!file.exists()) {
            return@withContext null
        }
        val content = file.readText()
        val formatList = format.toList()
        when (path.substringAfterLast('.', "").lowercase()) {
            formatList[0] -> parseLRC(content).copy(id = id, path = path)
            formatList[1] -> parseSRT(content).copy(id = id, path = path)
            else -> error("Unsupported lyrics format")
        }
    }

    /**
     * format
     *
     * [[00:00:01]]text1 text2
     *
     * or
     *
     * [[00:00.01]]text1 text2
     *
     * or
     *
     * [[00:00.01]][[00:00.02]]text1 text2
     *
     */
    suspend fun parseLRC(content: String): LyricsDocument {
        val rawLines = mutableListOf<Pair<Long, String>>()
        content.lineSequence().forEach { line ->
            val matches = lrcTimeRegex.findAll(line).toList()
            if (matches.isEmpty()) return@forEach
            val text = line.substringAfterLast("]").trim()
            if (text.isEmpty()) return@forEach
            matches.forEach { match ->
                val minutes = match.groupValues[1].toLong()
                val second = match.groupValues[2].toLong()
                val fraction = match.groupValues[3]
                val millis = when (fraction.length) {
                    0 -> 0L
                    1 -> fraction.toLong() * 100
                    2 -> fraction.toLong() * 10
                    else -> fraction.take(3).toLong()
                }
                val timeMs = minutes * 60_000L + second * 1_000L + millis
                rawLines += timeMs to text
            }
        }
        val sorted = rawLines.sortedBy { it.first }
        val lines = sorted.mapIndexed { index, item ->
            val start = item.first
            val end = sorted.getOrNull(index + 1)?.first
            LyricsLine(
                index = index,
                startTimeMs = start,
                endTimeMs = end,
                text = item.second
            )
        }

        return LyricsDocument(
            line = lines,
            timed = true
        )
    }

    /**
     * format
     *
     * 00:00:10,000 --> 00:00:12,300
     *
     * text
     */
    suspend fun parseSRT(content: String): LyricsDocument {
        val blocks = content.trim().split(Regex("""\r?\n\r?\n+"""))
        val lines = blocks.mapNotNull { block -> parseBlock(block) }
        return LyricsDocument(
            line = lines,
            timed = true
        )
    }

    private fun parseBlock(block: String): LyricsLine? {
        val lines = block.lines()
        val timeLine = lines.firstOrNull { srtTimeRegex.containsMatchIn(it) } ?: return null
        val match = srtTimeRegex.find(timeLine) ?: return null
        val start: Long = parseTime(
            match.groupValues[1],
            match.groupValues[2],
            match.groupValues[3],
            match.groupValues[4],
        )
        val end: Long = parseTime(
            match.groupValues[5],
            match.groupValues[6],
            match.groupValues[7],
            match.groupValues[8],
        )
        val timeIndex = lines.indexOf(timeLine)
        val text = lines.drop(timeIndex + 1).joinToString("\n").trim()
        if (text.isEmpty()) return null
        return LyricsLine(
            index = 0,
            startTimeMs = start,
            endTimeMs = end,
            text = text
        )
    }

    private fun parseTime(
        hour: String,
        minute: String,
        second: String,
        millis: String
    ): Long {
        return hour.toLong() * 3_600_000L + minute.toLong() * 60_000L + second.toLong() * 1_000L + millis.toLong()
    }
}