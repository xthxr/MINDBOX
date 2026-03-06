package com.example.mindbox.data.local.dao

import androidx.room.*
import com.example.mindbox.data.local.entity.OrganizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrganizationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(org: OrganizationEntity): Long

    @Update
    suspend fun update(org: OrganizationEntity)

    @Delete
    suspend fun delete(org: OrganizationEntity)

    @Query("SELECT * FROM organizations ORDER BY name ASC")
    fun getAllFlow(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE id = :id")
    suspend fun getById(id: Long): OrganizationEntity?

    @Query("SELECT * FROM organizations WHERE name LIKE '%' || :name || '%' LIMIT 20")
    suspend fun searchByName(name: String): List<OrganizationEntity>

    @Query("SELECT * FROM organizations WHERE name = :exactName LIMIT 1")
    suspend fun findExact(exactName: String): OrganizationEntity?

    @Query("SELECT * FROM organizations WHERE synced = 0")
    suspend fun getUnsynced(): List<OrganizationEntity>

    @Query("UPDATE organizations SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("DELETE FROM organizations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
