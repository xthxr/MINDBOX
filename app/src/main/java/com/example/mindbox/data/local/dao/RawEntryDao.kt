package com.example.mindbox.data.local.dao

import androidx.room.*
import com.example.mindbox.data.local.entity.RawEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RawEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RawEntryEntity): Long

    @Update
    suspend fun update(entry: RawEntryEntity)

    @Delete
    suspend fun delete(entry: RawEntryEntity)

    @Query("SELECT * FROM raw_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<RawEntryEntity>>

    @Query("SELECT * FROM raw_entries WHERE id = :id")
    suspend fun getById(id: Long): RawEntryEntity?

    @Query("SELECT * FROM raw_entries WHERE is_processed = 0 ORDER BY timestamp ASC")
    suspend fun getUnprocessed(): List<RawEntryEntity>

    @Query("UPDATE raw_entries SET is_processed = 1 WHERE id = :id")
    suspend fun markProcessed(id: Long)

    @Query("SELECT * FROM raw_entries WHERE synced = 0")
    suspend fun getUnsynced(): List<RawEntryEntity>

    @Query("UPDATE raw_entries SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM raw_entries")
    suspend fun count(): Int
}
