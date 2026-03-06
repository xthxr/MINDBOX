package com.example.mindbox.domain.repository

import com.example.mindbox.domain.model.*
import kotlinx.coroutines.flow.Flow

interface IEntryRepository {
    fun getAllEntriesFlow(): Flow<List<RawEntry>>
    suspend fun saveRawEntry(text: String, source: String = "TEXT"): Long
    suspend fun getById(id: Long): RawEntry?
    suspend fun markProcessed(id: Long)
    suspend fun getUnprocessed(): List<RawEntry>
    suspend fun getUnsynced(): List<RawEntry>
    suspend fun markSynced(ids: List<Long>)
    suspend fun count(): Int
}

interface IEventRepository {
    fun getAllEventsFlow(): Flow<List<Event>>
    suspend fun saveEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(id: Long)
    suspend fun getById(id: Long): Event?
    suspend fun searchStructured(
        query: String? = null,
        eventType: String? = null,
        orgId: Long? = null,
        dateFrom: Long? = null,
        dateTo: Long? = null,
        limit: Int = 50
    ): List<Event>
    suspend fun getUnsynced(): List<Event>
    suspend fun markSynced(ids: List<Long>)
    suspend fun getModifiedSince(since: Long): List<Event>
    fun getByOrgFlow(orgId: Long): Flow<List<Event>>
}

interface INoteRepository {
    fun getAllNotesFlow(): Flow<List<Note>>
    suspend fun saveNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun getById(id: Long): Note?
    suspend fun search(query: String? = null, limit: Int = 50): List<Note>
    suspend fun getUnsynced(): List<Note>
    suspend fun markSynced(ids: List<Long>)
}

interface IPeopleRepository {
    fun getAllPeopleFlow(): Flow<List<Person>>
    suspend fun savePerson(person: Person): Long
    suspend fun updatePerson(person: Person)
    suspend fun deletePerson(id: Long)
    suspend fun getById(id: Long): Person?
    suspend fun searchByName(name: String): List<Person>
    suspend fun findExact(name: String): Person?
    suspend fun getUnsynced(): List<Person>
    suspend fun markSynced(ids: List<Long>)
}

interface IOrgRepository {
    fun getAllOrgsFlow(): Flow<List<Organization>>
    suspend fun saveOrg(org: Organization): Long
    suspend fun updateOrg(org: Organization)
    suspend fun deleteOrg(id: Long)
    suspend fun getById(id: Long): Organization?
    suspend fun searchByName(name: String): List<Organization>
    suspend fun findExact(name: String): Organization?
    suspend fun getUnsynced(): List<Organization>
    suspend fun markSynced(ids: List<Long>)
}

interface IEmbeddingRepository {
    suspend fun saveEmbedding(entryId: Long, entryType: String, vector: FloatArray, modelVersion: String = "mobilebert_v1"): Long
    suspend fun getByEntry(entryId: Long, entryType: String): EmbeddingRecord?
    suspend fun getAllByType(entryType: String): List<EmbeddingRecord>
    suspend fun getAll(): List<EmbeddingRecord>
    suspend fun deleteByEntry(entryId: Long, entryType: String)
    suspend fun count(): Int
}
