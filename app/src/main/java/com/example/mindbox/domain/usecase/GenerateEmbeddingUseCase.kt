package com.example.mindbox.domain.usecase

import com.example.mindbox.domain.repository.IEmbeddingRepository
import com.example.mindbox.ml.EmbeddingModelManager
import javax.inject.Inject

class GenerateEmbeddingUseCase @Inject constructor(
    private val embeddingModelManager: EmbeddingModelManager,
    private val embeddingRepository: IEmbeddingRepository
) {
    /**
     * Generates and persists an embedding for [text] associated with [entryId]/[entryType].
     * Overwrites any existing embedding for the same entry.
     */
    suspend operator fun invoke(
        text: String,
        entryId: Long,
        entryType: String
    ): FloatArray {
        val vector = embeddingModelManager.embed(text)
        // Delete old embedding if exists
        embeddingRepository.deleteByEntry(entryId, entryType)
        // Persist new embedding
        embeddingRepository.saveEmbedding(
            entryId = entryId,
            entryType = entryType,
            vector = vector,
            modelVersion = EmbeddingModelManager.MODEL_VERSION
        )
        return vector
    }
}
