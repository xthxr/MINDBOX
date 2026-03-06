package com.example.mindbox.data.repository

import com.example.mindbox.data.local.dao.OrganizationDao
import com.example.mindbox.data.local.entity.OrganizationEntity
import com.example.mindbox.domain.model.Organization
import com.example.mindbox.domain.repository.IOrgRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrgRepository @Inject constructor(
    private val organizationDao: OrganizationDao
) : IOrgRepository {

    override fun getAllOrgsFlow(): Flow<List<Organization>> =
        organizationDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun saveOrg(org: Organization): Long =
        organizationDao.insert(org.toEntity())

    override suspend fun updateOrg(org: Organization) =
        organizationDao.update(org.toEntity())

    override suspend fun deleteOrg(id: Long) = organizationDao.deleteById(id)

    override suspend fun getById(id: Long): Organization? =
        organizationDao.getById(id)?.toDomain()

    override suspend fun searchByName(name: String): List<Organization> =
        organizationDao.searchByName(name).map { it.toDomain() }

    override suspend fun findExact(name: String): Organization? =
        organizationDao.findExact(name)?.toDomain()

    override suspend fun getUnsynced(): List<Organization> =
        organizationDao.getUnsynced().map { it.toDomain() }

    override suspend fun markSynced(ids: List<Long>) = organizationDao.markSynced(ids)

    private fun OrganizationEntity.toDomain() = Organization(
        id = id, name = name, aliases = aliases, orgType = orgType,
        timestamp = timestamp, synced = synced
    )

    private fun Organization.toEntity() = OrganizationEntity(
        id = id, name = name, aliases = aliases, orgType = orgType,
        timestamp = timestamp, synced = synced
    )
}
