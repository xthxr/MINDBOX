package com.example.mindbox.data.repository

import com.example.mindbox.data.local.dao.PersonDao
import com.example.mindbox.data.local.entity.PersonEntity
import com.example.mindbox.domain.model.Person
import com.example.mindbox.domain.repository.IPeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepository @Inject constructor(
    private val personDao: PersonDao
) : IPeopleRepository {

    override fun getAllPeopleFlow(): Flow<List<Person>> =
        personDao.getAllFlow().map { list -> list.map { it.toDomain() } }

    override suspend fun savePerson(person: Person): Long = personDao.insert(person.toEntity())

    override suspend fun updatePerson(person: Person) = personDao.update(person.toEntity())

    override suspend fun deletePerson(id: Long) = personDao.deleteById(id)

    override suspend fun getById(id: Long): Person? = personDao.getById(id)?.toDomain()

    override suspend fun searchByName(name: String): List<Person> =
        personDao.searchByName(name).map { it.toDomain() }

    override suspend fun findExact(name: String): Person? =
        personDao.findExact(name)?.toDomain()

    override suspend fun getUnsynced(): List<Person> =
        personDao.getUnsynced().map { it.toDomain() }

    override suspend fun markSynced(ids: List<Long>) = personDao.markSynced(ids)

    private fun PersonEntity.toDomain() = Person(
        id = id, name = name, aliases = aliases,
        linkedEventIds = linkedEventIds, timestamp = timestamp, synced = synced
    )

    private fun Person.toEntity() = PersonEntity(
        id = id, name = name, aliases = aliases,
        linkedEventIds = linkedEventIds, timestamp = timestamp, synced = synced
    )
}
