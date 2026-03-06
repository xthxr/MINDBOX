package com.example.mindbox.data.local.dao

import androidx.room.*
import com.example.mindbox.data.local.entity.EmbeddingEntity

@Dao
interface EmbeddingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(embedding: EmbeddingEntity): Long

    @Update
    suspend fun update(embedding: EmbeddingEntity)

    @Delete
    suspend fun delete(embedding: EmbeddingEntity)

    @Query("SELECT * FROM embeddings WHERE entry_id = :entryId AND entry_type = :entryType LIMIT 1")
    suspend fun getByEntry(entryId: Long, entryType: String): EmbeddingEntity?

    @Query("SELECT * FROM embeddings WHERE entry_type = :entryType")
    suspend fun getAllByType(entryType: String): List<EmbeddingEntity>

    @Query("SELECT * FROM embeddings")
    suspend fun getAll(): List<EmbeddingEntity>

    @Query("DELETE FROM embeddings WHERE entry_id = :entryId AND entry_type = :entryType")
    suspend fun deleteByEntry(entryId: Long, entryType: String)

    @Query("SELECT COUNT(*) FROM embeddings")
    suspend fun count(): Int
}
