package com.pointchange.sampleaudio.player.queue


class QueueRepository(private val dao: QueueDao) {
     fun getAll() = dao.observeAll()

    suspend fun update(name: String, queueName: String, value: String) {
        dao.update(name, queueName, value)
    }

    suspend fun updateQueueName(name: String, queueName: String) {
        dao.updateQueueName(name, queueName)
    }

    suspend fun deleteAll(users: List<String>) {
        dao.deleteAll(users)
    }
    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun insert(extras: QueueInfo) = dao.insert(extras)
    suspend fun insertAll(extras: List<QueueInfo>) = dao.insertAll(extras)
    suspend fun editOrAddQueue(queueInfos: List<QueueInfo>)= dao.editOrAddQueue(queueInfos)
    suspend fun deleteAllByQueueName(name: String, queueName: String) {
        dao.deleteAllByQueueName(name, queueName)
    }
    suspend fun deleteAllByQueueNames(name: String, queueNames: List<String>) {
        dao.deleteAllByQueueNames(name, queueNames)
    }

    suspend fun count() = dao.count()

    suspend fun deleteByName(name: String) {
        dao.deleteByName(name)
    }
}