package com.example.mindbox.nlp.confidence

import javax.inject.Inject

/**
 * Aggregates per-field confidence scores into an overall score.
 *
 * Weights:
 *   date       : 35%
 *   event_type : 25%
 *   org        : 25%
 *   people     : 15%
 */
class ExtractionConfidenceScorer @Inject constructor() {

    companion object {
        private const val W_DATE = 0.35f
        private const val W_EVENT = 0.25f
        private const val W_ORG = 0.25f
        private const val W_PEOPLE = 0.15f
    }

    /**
     * @param dateConfidence  0.0 if no date found
     * @param eventConfidence 0.0 if only "OTHER" matched
     * @param orgConfidence   0.0 if no org found
     * @param peopleConfidence 0.0 if no people found
     * @return overall confidence in [0.0, 1.0] and per-field map
     */
    fun score(
        dateConfidence: Float,
        eventConfidence: Float,
        orgConfidence: Float,
        peopleConfidence: Float
    ): ScoringResult {
        val overall = (dateConfidence * W_DATE +
                eventConfidence * W_EVENT +
                orgConfidence * W_ORG +
                peopleConfidence * W_PEOPLE).coerceIn(0f, 1f)

        return ScoringResult(
            overallConfidence = overall,
            fieldConfidences = mapOf(
                "date" to dateConfidence,
                "event_type" to eventConfidence,
                "org" to orgConfidence,
                "people" to peopleConfidence
            )
        )
    }

    data class ScoringResult(
        val overallConfidence: Float,
        val fieldConfidences: Map<String, Float>
    )
}
