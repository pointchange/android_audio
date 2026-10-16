package com.pointchange.sampleaudio.database

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.pointchange.sampleaudio.data.SetSongCoverInfo
import com.pointchange.sampleaudio.data.SongInfo
import com.pointchange.sampleaudio.data.SongInfoUpsert
import com.pointchange.sampleaudio.data.SongScanInfo
import kotlinx.coroutines.flow.Flow
import kotlin.collections.emptyList

class SongRepository(private val dao: SongDao) {
    //
//    fun songs(): Flow<PagingData<SongInfo>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = 40,
//                prefetchDistance = 5,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = {
//                dao.pagingSongs()
//            }
//        ).flow
//    }
//
//    fun playlist(list: List<Long>): Flow<PagingData<SongInfo>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = 40,
//                prefetchDistance = 5,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = {
//                dao.pagingPlaylist(list)
//            }
//        ).flow
//    }
    suspend fun getSongsCount(count:Int): List<SongInfo> = dao.getSongsCount(count)
    fun songs(): PagingSource<Int, SongInfo> = dao.pagingSongs()
    fun playlist(list: List<Long>): PagingSource<Int, SongInfo> = dao.pagingPlaylist(list)
    fun favorites(): PagingSource<Int, SongInfo> = dao.pagingFavoriteSongs()
    fun search(query: String): Flow<PagingData<SongInfo>> {
        return Pager(
            config = PagingConfig(
                pageSize = 40,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                dao.searchSongs(query)
            }
        ).flow
    }

    suspend fun searchTakeIds(query: String): List<Long> {
        return dao.searchSongsTakeIds(query)
    }


//    fun favorites(): Flow<PagingData<SongInfo>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = 40,
//                prefetchDistance = 5,
//                enablePlaceholders = false
//            ),
//            pagingSourceFactory = {
//                dao.pagingFavoriteSongs()
//            }
//        ).flow
//    }

    suspend fun getById(id: Long): SongInfo? = dao.getById(id)

    suspend fun getByPath(path: String): SongInfo? {
        return dao.getByPath(path)
    }

    suspend fun insertAll(songs: List<SongInfo>) {
        dao.insertAll(songs)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun deleteByPath(paths: List<String>) {
        if (paths.isNotEmpty())
            dao.deleteByPath(paths)
    }

    suspend fun deleteByIds(ids: List<Long>) {
        if (ids.isNotEmpty())
            dao.deleteByIds(ids)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun toggleFavorite(id: Long) {
        dao.toggleFavorite(id)
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) {
        dao.setFavorite(id, favorite)
    }

    suspend fun count(): Int {
        return dao.count()
    }

    fun countFlow(): Flow<Int> = dao.countFlow()

    fun getIds() = dao.getIds()
    fun getFavorIds() = dao.getFavorIds()
    suspend fun getFavorById(id: Long) = dao.getFavorById(id)
//    suspend fun updateScanInfo(song: SongScanInfo) {
//        dao.updateScanInfo(
//            title = song.title,
//            artist = song.artist,
//            album = song.album,
//            durationMs = song.durationMs,
//            fileSize = song.fileSize,
//            lastModified = song.lastModified
//        )
//    }

    suspend fun getScanInfos(): List<SongScanInfo> {
        return dao.getScanInfos()
    }

    suspend fun upsertScanInfos(songs: List<SongInfoUpsert>) {
        dao.upsertScanInfos(songs)
    }

    suspend fun updateCoverPath(setSongCoverInfo: SetSongCoverInfo) {
        dao.updateCoverPath(setSongCoverInfo.path, setSongCoverInfo.coverPath)
    }

    suspend fun getSongWithoutCover(limit: Int) = dao.getSongWithoutCover(limit)

    suspend fun getByIds(ids: List<Long>) = dao.getByIds(ids)

    suspend fun getCoverPath(paths: List<String>) = dao.getCoverPath(paths)
    suspend fun getAllSongInfo() = dao.getAllSongInfo()
}