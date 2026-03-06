package com.example.mindbox.data.repository

import com.example.mindbox.data.local.dao.NoteDao
import com.example.mindbox.data.local.entity.NoteEntity
import com.example.mindbox.domain.model.Note
import com.example.mindbox.domain.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) : INoteRepository {

    override fun getAllNotesFlow(): Flow<List<Note>> =
        noteDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveNote(note: Note): Long = noteDao.insert(note.toEntity())

    override suspend fun updateNote(note: Note) = noteDao.update(note.toEntity())

    override suspend fun deleteNote(id: Long) = noteDao.deleteById(id)

    override suspend fun getById(id: Long): Note? = noteDao.getById(id)?.toDomain()

    override suspend fun search(query: String?, limit: Int): List<Note> =
        noteDao.search(query, limit).map { it.toDomain() }

    override suspend fun getUnsynced(): List<Note> =
        noteDao.getUnsynced().map { it.toDomain() }

    override suspend fun markSynced(ids: List<Long>) = noteDao.markSynced(ids)

    private fun NoteEntity.toDomain() = Note(
        id = id, rawEntryId = rawEntryId, content = content,
        tags = tags, timestamp = timestamp, lastModified = lastModified, synced = synced
    )

    private fun Note.toEntity() = NoteEntity(
        id = id, rawEntryId = rawEntryId, content = content,
        tags = tags, timestamp = timestamp, lastModified = lastModified, synced = synced
    )
}
