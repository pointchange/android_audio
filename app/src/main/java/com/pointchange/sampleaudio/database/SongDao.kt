package com.pointchange.sampleaudio.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.pointchange.sampleaudio.data.GetSongCoverInfo
import com.pointchange.sampleaudio.data.SongInfo
import com.pointchange.sampleaudio.data.SongInfoItemLyrics
import com.pointchange.sampleaudio.data.SongInfoItemQueue
import com.pointchange.sampleaudio.data.SongInfoUpsert
import com.pointchange.sampleaudio.data.SongScanInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    /**
     * 所有歌曲
     */
    @Query(
        """
        SELECT * FROM songs
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    fun pagingSongs(): PagingSource<Int, SongInfo>

    @Query(
        """
        SELECT * FROM songs
        WHERE id IN (:list)
    """
    )
    fun pagingPlaylist(list: List<Long>): PagingSource<Int, SongInfo>

    @Query(
        """
        SELECT id, path, fileSize, lastModified FROM songs
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    suspend fun getScanInfos(): List<SongScanInfo>

    /**
     * 搜索
     */
    @Query(
        """
        SELECT * FROM songs
        WHERE title LIKE '%' || :query || '%'
           OR artist LIKE '%' || :query || '%'
           OR album LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    fun searchSongs(query: String): PagingSource<Int, SongInfo>

    @Query(
        """
        SELECT id FROM songs
        WHERE title LIKE '%' || :query || '%'
           OR artist LIKE '%' || :query || '%'
           OR album LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    suspend fun searchSongsTakeIds(query: String): List<Long>

    /**
     * 收藏歌曲
     */
    @Query(
        """
        SELECT * FROM songs
        WHERE isFavorite = 1
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    fun pagingFavoriteSongs(): PagingSource<Int, SongInfo>

    @Query(
        """
        SELECT id FROM songs
        WHERE isFavorite = 1
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    fun getFavorIds(): List<Long>

    @Query(
        """
        SELECT isFavorite FROM songs
        WHERE id = :id
    """
    )
    suspend fun getFavorById(id: Long): Boolean

    /**
     * 根据 ID 查询
     */
    @Query(
        """
        SELECT * FROM songs
        WHERE id = :id
        LIMIT 1
    """
    )
    suspend fun getById(id: Long): SongInfo?

    /**
     * 根据路径查询
     */
    @Query(
        """
        SELECT * FROM songs
        WHERE path = :path
        LIMIT 1
    """
    )
    suspend fun getByPath(path: String): SongInfo?

    /**
     * 新增
     */
    @Insert
    suspend fun insert(song: SongInfo)

    /**
     * 批量新增
     */
    @Insert
    suspend fun insertAll(songs: List<SongInfo>)
//
//    @Query(
//        """
//        UPDATE songs
//        SET title = :title,
//            artist = :artist,
//            album = :album,
//            durationMs = :durationMs,
//            fileSize = :fileSize,
//            lastModified = :lastModified
//        WHERE path = :
//    """
//    )
//    suspend fun updateScanInfo(
//        path:String,
//        title: String?,
//        artist: String?,
//        album: String?,
//        durationMs: Long,
//        fileSize: Long,
//        lastModified: Long,
//    )

    @Upsert(entity = SongInfo::class)
    suspend fun upsertScanInfos(songs: List<SongInfoUpsert>)

    /**
     * 删除
     */
    @Delete
    suspend fun delete(song: SongInfo)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM songs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM songs WHERE path IN (:paths)")
    suspend fun deleteByPath(paths: List<String>)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    /**
     * 收藏 / 取消收藏
     */
    @Query(
        """
        UPDATE songs
        SET isFavorite = :favorite
        WHERE id = :id
    """
    )
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query(
        """
        UPDATE songs
        SET isFavorite = NOT isFavorite
        WHERE id = :id
    """
    )
    suspend fun toggleFavorite(id: Long)

    /**
     * 数量
     */
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM songs")
    fun countFlow(): Flow<Int>

    @Query(
        """
        SELECT id FROM songs
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    fun getIds(): Flow<List<Long>>

    @Query(
        """
        UPDATE songs
        SET coverPath = :coverPath,
            coverResolved = 1
        WHERE path = :path
    """
    )
    suspend fun updateCoverPath(path: String, coverPath: String?)

    @Query(
        """
        SELECT id, path
        FROM songs
        WHERE coverResolved = 0
        LIMIT :limit
        """
    )
    suspend fun getSongWithoutCover(limit: Int): List<GetSongCoverInfo>

    @Query(
        """
          SELECT coverPath FROM songs
          WHERE path IN (:paths)
    """
    )
    suspend fun getCoverPath(paths: List<String>): List<String?>

    @Query(
        """
        SELECT id, path, title, artist, album, durationMs FROM songs
        WHERE id IN (:ids)
        ORDER BY title COLLATE NOCASE ASC
    """
    )
    suspend fun getByIds(ids: List<Long>): List<SongInfoItemQueue>

    @Query("SELECT id, path FROM songs")
    suspend fun getAllSongInfo(): List<SongInfoItemLyrics>

    @Query("SELECT * FROM songs LIMIT :count")

    suspend fun getSongsCount(count: Int): List<SongInfo>
}

