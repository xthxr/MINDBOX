package com.example.mindbox.domain.usecase

import com.example.mindbox.query.QueryParser
import com.example.mindbox.query.QueryResponse
import com.example.mindbox.query.QueryRouter
import javax.inject.Inject

class QueryMemoryUseCase @Inject constructor(
    private val queryParser: QueryParser,
    private val queryRouter: QueryRouter
) {
    suspend operator fun invoke(question: String): QueryResponse {
        require(question.isNotBlank()) { "Query cannot be blank" }
        val parsed = queryParser.parse(question.trim())
        return queryRouter.route(parsed)
    }
}
