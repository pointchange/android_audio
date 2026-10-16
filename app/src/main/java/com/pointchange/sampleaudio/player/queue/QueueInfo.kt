package com.pointchange.sampleaudio.player.queue

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "queue_info", indices = [Index(value = ["id", "name"], unique = true)])
data class QueueInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "queue_name")
    val queueName: String,
    val value: String
)


//data class QueueInfoMemory(
//    val id: Int = 0,
//    val user: String = "a",
//    val queueName: String,
//    val list: List<Int> = emptyList()
//)
