package com.example.mindbox.data.repository

import com.example.mindbox.data.local.dao.EmbeddingDao
import com.example.mindbox.data.local.entity.EmbeddingEntity
import com.example.mindbox.domain.model.EmbeddingRecord
import com.example.mindbox.domain.repository.IEmbeddingRepository
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmbeddingRepository @Inject constructor(
    private val embeddingDao: EmbeddingDao
) : IEmbeddingRepository {

    override suspend fun saveEmbedding(
        entryId: Long,
        entryType: String,
        vector: FloatArray,
        modelVersion: String
    ): Long {
        val entity = EmbeddingEntity(
            entryId = entryId,
            entryType = entryType,
            vector = floatArrayToBytes(vector),
            modelVersion = modelVersion
        )
        return embeddingDao.insert(entity)
    }

    override suspend fun getByEntry(entryId: Long, entryType: String): EmbeddingRecord? =
        embeddingDao.getByEntry(entryId, entryType)?.toDomain()

    override suspend fun getAllByType(entryType: String): List<EmbeddingRecord> =
        embeddingDao.getAllByType(entryType).map { it.toDomain() }

    override suspend fun getAll(): List<EmbeddingRecord> =
        embeddingDao.getAll().map { it.toDomain() }

    override suspend fun deleteByEntry(entryId: Long, entryType: String) =
        embeddingDao.deleteByEntry(entryId, entryType)

    override suspend fun count(): Int = embeddingDao.count()

    private fun EmbeddingEntity.toDomain() = EmbeddingRecord(
        id = id, entryId = entryId, entryType = entryType,
        vector = bytesToFloatArray(vector), modelVersion = modelVersion,
        timestamp = timestamp
    )

    private fun floatArrayToBytes(arr: FloatArray): ByteArray {
        val buf = ByteBuffer.allocate(arr.size * 4)
        arr.forEach { buf.putFloat(it) }
        return buf.array()
    }

    private fun bytesToFloatArray(bytes: ByteArray): FloatArray {
        val buf = ByteBuffer.wrap(bytes)
        return FloatArray(bytes.size / 4) { buf.getFloat() }
    }
}
