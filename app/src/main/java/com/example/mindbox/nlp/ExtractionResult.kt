package com.example.mindbox.nlp

/**
 * Output of the full metadata extraction pipeline.
 */
data class ExtractionResult(
    val rawText: String,
    val title: String? = null,
    val eventType: String? = null,
    val dateEpoch: Long? = null,
    val dateRaw: String? = null,
    val orgName: String? = null,
    val people: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val confidence: Float = 0f,          // 0.0 – 1.0 overall
    val fieldConfidences: Map<String, Float> = emptyMap(), // per-field scores
    val usedNer: Boolean = false
)

enum class ConfidenceTier {
    HIGH,   // >= 0.8  → auto-save
    MEDIUM, // 0.5–0.8 → show confirmation card
    LOW     // < 0.5   → NER fallback, then user confirmation
}

fun ExtractionResult.confidenceTier(): ConfidenceTier = when {
    confidence >= 0.8f -> ConfidenceTier.HIGH
    confidence >= 0.5f -> ConfidenceTier.MEDIUM
    else -> ConfidenceTier.LOW
}
