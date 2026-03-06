package com.example.mindbox.data.repository

import com.example.mindbox.data.local.dao.RawEntryDao
import com.example.mindbox.data.local.entity.RawEntryEntity
import com.example.mindbox.domain.model.RawEntry
import com.example.mindbox.domain.repository.IEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(
    private val rawEntryDao: RawEntryDao
) : IEntryRepository {

    override fun getAllEntriesFlow(): Flow<List<RawEntry>> =
        rawEntryDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveRawEntry(text: String, source: String): Long {
        val entity = RawEntryEntity(rawText = text, inputSource = source)
        return rawEntryDao.insert(entity)
    }

    override suspend fun getById(id: Long): RawEntry? =
        rawEntryDao.getById(id)?.toDomain()

    override suspend fun markProcessed(id: Long) = rawEntryDao.markProcessed(id)

    override suspend fun getUnprocessed(): List<RawEntry> =
        rawEntryDao.getUnprocessed().map { it.toDomain() }

    override suspend fun getUnsynced(): List<RawEntry> =
        rawEntryDao.getUnsynced().map { it.toDomain() }

    override suspend fun markSynced(ids: List<Long>) = rawEntryDao.markSynced(ids)

    override suspend fun count(): Int = rawEntryDao.count()

    private fun RawEntryEntity.toDomain() = RawEntry(
        id = id,
        rawText = rawText,
        timestamp = timestamp,
        isProcessed = isProcessed,
        inputSource = inputSource
    )
}
