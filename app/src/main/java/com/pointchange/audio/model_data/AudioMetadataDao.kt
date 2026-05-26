package com.pointchange.audio.model_data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioMetadataDao {

    @Query("SELECT * FROM audio_metadata")
    fun getAllAudioFlow(): Flow<List<AudioMetadata>>

    @Query("SELECT * FROM audio_metadata WHERE uri = :uri LIMIT 1")
    suspend fun getMetadata(uri: String): AudioMetadata?

    @Upsert
    suspend fun saveMetadata(metadata: AudioMetadata)


    @Query("SELECT * FROM audio_metadata ORDER BY uri ASC")
    fun getAllAudioPaged(): PagingSource<Int, AudioMetadata>

    @Query("SELECT * FROM audio_metadata ORDER BY uri ASC")
    suspend fun getAllAudio(): List<AudioMetadata>

    @Query("SELECT * FROM audio_metadata WHERE title LIKE '%' || :keyword || '%' OR artist LIKE '%' || :keyword || '%' OR album LIKE '%' || :keyword || '%' OR uri LIKE '%' || :keyword || '%' ORDER BY uri ASC")
    fun searchMetaData(keyword: String): PagingSource<Int, AudioMetadata>


    @Query(
        """
         SELECT * FROM audio_metadata ORDER BY
         CASE :sortOrder
            WHEN 'title' THEN title
            WHEN 'artist' THEN artist
            ELSE uri
         END ASC, uri ASC       
     """
    )
    fun getSortByAscMetaData(sortOrder: String): PagingSource<Int, AudioMetadata>


    @Update(entity = AudioMetadata::class)
    suspend fun updateAudioItem(audioItem: AudioItem)

    @Upsert
    fun upsertMetadata(metadata: AudioMetadata)

    @Query("UPDATE audio_metadata SET isFavorite = :isFav WHERE uri = :uri")
    suspend fun updateFavoriteState(uri: String, isFav: Boolean)

    @Query("UPDATE audio_metadata SET isFavorite = :isFav WHERE uri IN (:list)")
    suspend fun updateFavoriteListState(list: List<String>, isFav: Boolean)

    @Query("SELECT * FROM audio_metadata WHERE (:isInit = 0 OR isFavorite = 1) ORDER BY uri ASC")
    fun getFavoriteAudioMetadataPaged(isInit: Boolean): PagingSource<Int, AudioMetadata>

    @Query("DELETE FROM audio_metadata WHERE uri IN (:list)")
    suspend fun deleteMetadata(list: List<String>)

    @Query("DELETE FROM audio_metadata WHERE uri NOT IN (:list)")
    suspend fun deleteExceptMetadata(list: List<String>)

    @Query("DELETE FROM audio_metadata WHERE uri = :uri")
    suspend fun deleteOneMetadata(uri: String)

    @Upsert
    suspend fun saveMetadata(metadataList: List<AudioMetadata>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOnlyNews(list: List<AudioMetadata>)

    @Query("DELETE FROM audio_metadata")
    suspend fun clearCache()



    @Query("UPDATE audio_metadata SET lrc = :lrc WHERE uri = :uri")
    suspend fun updateLrc(uri: String, lrc: String)
//
//    @Query("SELECT lrc FROM audio_metadata ORDER BY uri")
//    suspend fun updatedToGetLrc(uri: String):String
}