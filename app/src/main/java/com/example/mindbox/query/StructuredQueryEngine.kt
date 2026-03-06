package com.example.mindbox.query

import com.example.mindbox.domain.model.Event
import com.example.mindbox.domain.model.Note
import com.example.mindbox.domain.repository.IEventRepository
import com.example.mindbox.domain.repository.INoteRepository
import com.example.mindbox.domain.repository.IOrgRepository
import javax.inject.Inject
import javax.inject.Singleton

sealed class MemoryResult {
    abstract val sourceTable: String
    abstract val sourceId: Long
    abstract val displayText: String
    abstract val timestamp: Long
    abstract val relevanceScore: Float

    data class EventResult(
        val event: Event,
        override val relevanceScore: Float = 1f,
        val resolvedOrgName: String? = null
    ) : MemoryResult() {
        override val sourceTable = "events"
        override val sourceId = event.id
        override val displayText = event.title
        override val timestamp = event.timestamp
    }

    data class NoteResult(
        val note: Note,
        override val relevanceScore: Float = 1f
    ) : MemoryResult() {
        override val sourceTable = "notes"
        override val sourceId = note.id
        override val displayText = note.content.take(120)
        override val timestamp = note.timestamp
    }
}

enum class SearchSource { STRUCTURED, SEMANTIC }

data class QueryResponse(
    val results: List<MemoryResult>,
    val source: SearchSource,
    val queryMs: Long
)

/**
 * Executes structured SQL queries against Room based on a [ParsedQuery].
 */
@Singleton
class StructuredQueryEngine @Inject constructor(
    private val eventRepository: IEventRepository,
    private val noteRepository: INoteRepository,
    private val orgRepository: IOrgRepository
) {
    suspend fun query(parsed: ParsedQuery): List<MemoryResult> {
        val results = mutableListOf<MemoryResult>()

        // Resolve org name to ID if present
        val orgId: Long? = parsed.orgHint?.let {
            orgRepository.searchByName(it).firstOrNull()?.id
        }

        // Build a keyword string for LIKE queries
        val keyword = parsed.keywords.take(3).joinToString(" ")

        when (parsed.intent) {
            QueryIntent.DATE_QUERY, QueryIntent.EVENT_QUERY, QueryIntent.ORG_QUERY -> {
                val events = eventRepository.searchStructured(
                    query = keyword.ifBlank { null },
                    eventType = parsed.eventType,
                    orgId = orgId,
                    dateFrom = parsed.dateFrom,
                    dateTo = parsed.dateTo,
                    limit = 20
                )
                events.forEach { event ->
                    val orgName = event.orgId?.let { orgRepository.getById(it)?.name }
                    results += MemoryResult.EventResult(event, 1.0f, orgName)
                }
            }
            QueryIntent.NOTE_QUERY, QueryIntent.GENERAL -> {
                val notes = noteRepository.search(
                    query = keyword.ifBlank { null },
                    limit = 20
                )
                notes.forEach { note -> results += MemoryResult.NoteResult(note) }

                // Also search events for general queries
                val events = eventRepository.searchStructured(
                    query = keyword.ifBlank { null },
                    limit = 10
                )
                events.forEach { event ->
                    val orgName = event.orgId?.let { orgRepository.getById(it)?.name }
                    results += MemoryResult.EventResult(event, 0.8f, orgName)
                }
            }
            QueryIntent.PEOPLE_QUERY -> {
                // People queries: search events for name mentions in notes/title
                val events = eventRepository.searchStructured(
                    query = keyword.ifBlank { null },
                    limit = 20
                )
                events.forEach { event ->
                    val orgName = event.orgId?.let { orgRepository.getById(it)?.name }
                    results += MemoryResult.EventResult(event, 1.0f, orgName)
                }
            }
        }

        return results.sortedByDescending { it.timestamp }
    }
}
