package com.example.mindbox.data.local.dao

import androidx.room.*
import com.example.mindbox.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY date DESC, timestamp DESC")
    fun getAllFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<EventEntity>

    @Query("""
        SELECT * FROM events
        WHERE (:query IS NULL OR title LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%')
        AND (:eventType IS NULL OR event_type = :eventType)
        AND (:orgId IS NULL OR org_id = :orgId)
        AND (:dateFrom IS NULL OR date >= :dateFrom)
        AND (:dateTo IS NULL OR date <= :dateTo)
        ORDER BY date DESC, timestamp DESC
        LIMIT :limit
    """)
    suspend fun searchStructured(
        query: String? = null,
        eventType: String? = null,
        orgId: Long? = null,
        dateFrom: Long? = null,
        dateTo: Long? = null,
        limit: Int = 50
    ): List<EventEntity>

    @Query("SELECT * FROM events WHERE org_id = :orgId ORDER BY date DESC")
    fun getByOrg(orgId: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE synced = 0")
    suspend fun getUnsynced(): List<EventEntity>

    @Query("UPDATE events SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT * FROM events WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getModifiedSince(since: Long): List<EventEntity>

    @Query("SELECT COUNT(*) FROM events")
    suspend fun count(): Int

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: Long)
}
