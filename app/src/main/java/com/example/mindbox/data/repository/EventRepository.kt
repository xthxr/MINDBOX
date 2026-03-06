package com.example.mindbox.data.repository

import com.example.mindbox.data.local.dao.EventDao
import com.example.mindbox.data.local.entity.EventEntity
import com.example.mindbox.domain.model.Event
import com.example.mindbox.domain.repository.IEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val eventDao: EventDao
) : IEventRepository {

    override fun getAllEventsFlow(): Flow<List<Event>> =
        eventDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveEvent(event: Event): Long {
        return eventDao.insert(event.toEntity())
    }

    override suspend fun updateEvent(event: Event) = eventDao.update(event.toEntity())

    override suspend fun deleteEvent(id: Long) = eventDao.deleteById(id)

    override suspend fun getById(id: Long): Event? = eventDao.getById(id)?.toDomain()

    override suspend fun searchStructured(
        query: String?,
        eventType: String?,
        orgId: Long?,
        dateFrom: Long?,
        dateTo: Long?,
        limit: Int
    ): List<Event> = eventDao.searchStructured(query, eventType, orgId, dateFrom, dateTo, limit)
        .map { it.toDomain() }

    override suspend fun getUnsynced(): List<Event> =
        eventDao.getUnsynced().map { it.toDomain() }

    override suspend fun markSynced(ids: List<Long>) = eventDao.markSynced(ids)

    override suspend fun getModifiedSince(since: Long): List<Event> =
        eventDao.getModifiedSince(since).map { it.toDomain() }

    override fun getByOrgFlow(orgId: Long): Flow<List<Event>> =
        eventDao.getByOrg(orgId).map { list -> list.map { it.toDomain() } }

    private fun EventEntity.toDomain() = Event(
        id = id, rawEntryId = rawEntryId, title = title, eventType = eventType,
        date = date, dateRaw = dateRaw, orgId = orgId, tags = tags,
        notes = notes, peopleIds = peopleIds, timestamp = timestamp,
        lastModified = lastModified, synced = synced
    )

    private fun Event.toEntity() = EventEntity(
        id = id, rawEntryId = rawEntryId, title = title, eventType = eventType,
        date = date, dateRaw = dateRaw, orgId = orgId, tags = tags,
        notes = notes, peopleIds = peopleIds, timestamp = timestamp,
        lastModified = lastModified, synced = synced
    )
}
