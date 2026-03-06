package com.example.mindbox.data.local.dao

import androidx.room.*
import com.example.mindbox.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("""
        SELECT * FROM notes
        WHERE (:query IS NULL OR content LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun search(query: String? = null, limit: Int = 50): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE synced = 0")
    suspend fun getUnsynced(): List<NoteEntity>

    @Query("UPDATE notes SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT * FROM notes WHERE timestamp > :since")
    suspend fun getModifiedSince(since: Long): List<NoteEntity>

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
