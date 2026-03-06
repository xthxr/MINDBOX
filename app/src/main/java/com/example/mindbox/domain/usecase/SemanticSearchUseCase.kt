package com.example.mindbox.domain.usecase

import com.example.mindbox.ml.EmbeddingModelManager
import com.example.mindbox.ml.SemanticSearchEngine
import com.example.mindbox.ml.SemanticSearchResult
import javax.inject.Inject

class SemanticSearchUseCase @Inject constructor(
    private val embeddingModelManager: EmbeddingModelManager,
    private val semanticSearchEngine: SemanticSearchEngine
) {
    suspend operator fun invoke(
        queryText: String,
        topK: Int = 10,
        entryType: String? = null
    ): List<SemanticSearchResult> {
        val queryVector = embeddingModelManager.embed(queryText)
        return semanticSearchEngine.search(queryVector, topK, entryType)
    }
}
