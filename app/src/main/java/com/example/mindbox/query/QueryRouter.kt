package com.example.mindbox.query

import com.example.mindbox.domain.repository.IEventRepository
import com.example.mindbox.domain.repository.INoteRepository
import com.example.mindbox.domain.usecase.SemanticSearchUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routes a parsed query:
 * 1. Executes structured SQL via [StructuredQueryEngine]
 * 2. If 0 results found, falls back to semantic search via [SemanticSearchUseCase]
 * 3. Merges and ranks results; attaches source label (STRUCTURED | SEMANTIC)
 */
@Singleton
class QueryRouter @Inject constructor(
    private val structuredQueryEngine: StructuredQueryEngine,
    private val semanticSearchUseCase: SemanticSearchUseCase,
    private val eventRepository: IEventRepository,
    private val noteRepository: INoteRepository
) {
    suspend fun route(parsed: ParsedQuery): QueryResponse {
        val start = System.currentTimeMillis()

        // Attempt structured search first
        val structuredResults = structuredQueryEngine.query(parsed)

        if (structuredResults.isNotEmpty()) {
            return QueryResponse(
                results = structuredResults,
                source = SearchSource.STRUCTURED,
                queryMs = System.currentTimeMillis() - start
            )
        }

        // Fall back to semantic search
        val semanticResults = semanticSearchUseCase(
            queryText = parsed.raw,
            topK = 15
        )

        // Resolve semantic results to domain objects
        val resolved = semanticResults.mapNotNull { hit ->
            when (hit.record.entryType) {
                "EVENT" -> {
                    val event = eventRepository.getById(hit.record.entryId)
                    event?.let { MemoryResult.EventResult(it, hit.score) }
                }
                "NOTE" -> {
                    val note = noteRepository.getById(hit.record.entryId)
                    note?.let { MemoryResult.NoteResult(it, hit.score) }
                }
                else -> null
            }
        }

        return QueryResponse(
            results = resolved,
            source = SearchSource.SEMANTIC,
            queryMs = System.currentTimeMillis() - start
        )
    }
}
