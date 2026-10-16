package com.pointchange.sampleaudio.player.queue

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(extras: List<QueueInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extras: QueueInfo): Long

    @Upsert
    suspend fun editOrAddQueue(queueInfos: List<QueueInfo>)

    //someone queue
    @Query(
        """
        DELETE FROM queue_info
        WHERE name = :name AND queue_name = :queueName
    """
    )
    suspend fun deleteAllByQueueName(name: String, queueName: String)

    @Query(
        """
        DELETE FROM queue_info
        WHERE name = :name AND queue_name IN (:queueNames)
    """
    )
    suspend fun deleteAllByQueueNames(name: String, queueNames: List<String>)

    @Query(
        """
        DELETE FROM queue_info
        WHERE name = :name 
    """
    )
    suspend fun deleteByName(name: String)


    @Query(
        """
        SELECT * FROM queue_info
        WHERE name = :name
    """
    )
    suspend fun getAll(name: String): List<QueueInfo>

    @Query(
        """
        SELECT * FROM queue_info
        WHERE name = :name AND queue_name = :queueName
        LIMIT 1
    """
    )
    suspend fun get(name: String, queueName: String): QueueInfo

    @Query(
        """
        UPDATE queue_info
        SET value = :value
        WHERE name = :name AND queue_name = :queueName
    """
    )
    suspend fun update(name: String, queueName: String, value: String)


    @Query(
        """
        UPDATE queue_info
        SET queue_name = :queueName
        WHERE name = :name 
    """
    )
    suspend fun updateQueueName(name: String, queueName: String)

    @Query(
        """
        SELECT * FROM queue_info
        WHERE name = :name AND queue_name = queue_name
    """
    )
    fun observeAllByName(name: String): Flow<List<QueueInfo>>


    @Query(
        """
        SELECT * FROM queue_info
    """
    )
    fun observeAll(): Flow<List<QueueInfo>>

    @Query(
        """
            DELETE FROM queue_info
            WHERE name NOT IN (:names)
        """
    )
    suspend fun deleteAll(names: List<String>)

    @Query("""DELETE FROM queue_info""")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM queue_info")
    suspend fun count(): Int

//    @Query(
//        """
//        SELECT * FROM queue_info
//        WHERE id = :id
//        LIMIT 1
//    """
//    )
//    suspend fun getById(id: Int): QueueInfo?
//
//    @Query("SELECT COUNT(*) FROM queue_info")
//    suspend fun count(): Int
//
//    @Query("SELECT * FROM queue_info")
//    suspend fun getAll(): List<QueueDao>
//
//    @Query("SELECT value FROM queue_info")
//    suspend fun getAllValue(): List<Int>
//
//    @Query(
//        """
//        UPDATE queue_info
//        SET value = :value
//        WHERE id = :id
//    """
//    )
//    suspend fun save(id: Int, value: Int)
}