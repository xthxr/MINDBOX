package com.example.mindbox.ml

import com.example.mindbox.domain.model.EmbeddingRecord
import com.example.mindbox.domain.repository.IEmbeddingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

data class SemanticSearchResult(
    val record: EmbeddingRecord,
    val score: Float
)

/**
 * Brute-force cosine similarity search over all stored embedding vectors.
 * Suitable for personal-scale data (< 10k entries).
 *
 * Upgrade path: swap [searchAll] for an ANN library (e.g., FAISS, Annoy via JNI)
 * when corpus grows beyond ~10k entries.
 */
@Singleton
class SemanticSearchEngine @Inject constructor(
    private val embeddingRepository: IEmbeddingRepository
) {
    /**
     * Find [topK] entries most similar to [queryVector].
     * @param entryType optional filter: "RAW_ENTRY" | "EVENT" | "NOTE" | null (all)
     */
    suspend fun search(
        queryVector: FloatArray,
        topK: Int = 10,
        entryType: String? = null
    ): List<SemanticSearchResult> {
        val corpus = if (entryType != null) {
            embeddingRepository.getAllByType(entryType)
        } else {
            embeddingRepository.getAll()
        }

        return corpus
            .map { record ->
                val similarity = cosineSimilarity(queryVector, record.vector)
                SemanticSearchResult(record, similarity)
            }
            .sortedByDescending { it.score }
            .take(topK)
    }

    /**
     * Cosine similarity between two L2-normalized vectors.
     * Since vectors are normalized, this is equivalent to dot product.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom < 1e-8f) 0f else dot / denom
    }
}
