package com.pointchange.sampleaudio.player.lyrics

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pointchange.sampleaudio.player.queue.QueueInfo

@Dao
interface LyricsDao {

    @Query(
        """
        SELECT * FROM lyrics
        WHERE songId = :songId
    """
    )
    suspend fun getBySongId(
        songId: Long
    ): LyricsEntity?


    @Query(
        """
        SELECT * FROM lyrics
    """
    )
    suspend fun getAll(): List<LyricsEntity>


    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insert(
        lyrics: LyricsEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lyrics: List<LyricsEntity>)

    @Update
    suspend fun update(
        lyrics: LyricsEntity
    )


    @Delete
    suspend fun delete(
        lyrics: LyricsEntity
    )


    @Query(
        """
        DELETE FROM lyrics
        WHERE songId = :songId
    """
    )
    suspend fun deleteBySongId(
        songId: Long
    )


    @Query(
        """
        DELETE FROM lyrics
    """
    )
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM lyrics")
    suspend fun count(): Int
}

