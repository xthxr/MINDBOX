package com.example.mindbox.domain.usecase

import com.example.mindbox.domain.model.Event
import com.example.mindbox.domain.repository.IEventRepository
import com.example.mindbox.domain.repository.INoteRepository
import com.example.mindbox.domain.repository.IPeopleRepository
import com.example.mindbox.domain.repository.IOrgRepository
import com.example.mindbox.nlp.ExtractionResult
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SaveStructuredEntryUseCase @Inject constructor(
    private val eventRepository: IEventRepository,
    private val noteRepository: INoteRepository,
    private val peopleRepository: IPeopleRepository,
    private val orgRepository: IOrgRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend operator fun invoke(
        rawEntryId: Long,
        result: ExtractionResult
    ): SaveResult {
        // Upsert organization
        val orgId: Long? = result.orgName?.let { orgName ->
            orgRepository.findExact(orgName)?.id
                ?: orgRepository.saveOrg(
                    com.example.mindbox.domain.model.Organization(name = orgName)
                )
        }

        // Upsert people
        val savedPeopleIds = result.people.map { personName ->
            peopleRepository.findExact(personName)?.id
                ?: peopleRepository.savePerson(
                    com.example.mindbox.domain.model.Person(name = personName)
                )
        }

        // Save event
        val eventId = eventRepository.saveEvent(
            Event(
                rawEntryId = rawEntryId,
                title = result.title ?: result.rawText.take(80),
                eventType = result.eventType ?: "OTHER",
                date = result.dateEpoch,
                dateRaw = result.dateRaw,
                orgId = orgId,
                tags = json.encodeToString(result.tags),
                notes = result.rawText,
                peopleIds = json.encodeToString(savedPeopleIds)
            )
        )

        return SaveResult(eventId = eventId, orgId = orgId, peopleIds = savedPeopleIds)
    }

    data class SaveResult(
        val eventId: Long,
        val orgId: Long?,
        val peopleIds: List<Long>
    )
}
