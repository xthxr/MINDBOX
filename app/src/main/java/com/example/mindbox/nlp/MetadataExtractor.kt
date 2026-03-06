package com.example.mindbox.nlp

import com.example.mindbox.nlp.confidence.ExtractionConfidenceScorer
import com.example.mindbox.nlp.extractor.*
import com.example.mindbox.nlp.ner.NERModelRunner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates the 3-tier NLP pipeline:
 * Tier 1: Rule-based extraction (date, event type, org, people)
 * Tier 2: Confidence scoring → HIGH auto-saves, MEDIUM shows card, LOW triggers NER
 * Tier 3: NER fallback to fill missing fields when confidence < 0.5
 */
@Singleton
class MetadataExtractor @Inject constructor(
    private val dateExtractor: DateExtractor,
    private val eventTypeExtractor: EventTypeExtractor,
    private val orgNameExtractor: OrgNameExtractor,
    private val peopleExtractor: PeopleExtractor,
    private val confidenceScorer: ExtractionConfidenceScorer,
    private val nerModelRunner: NERModelRunner
) {
    fun extract(text: String): ExtractionResult {
        // ---- Tier 1: Rule-based ----
        val dates = dateExtractor.extract(text)
        val topDate = dates.maxByOrNull { it.confidence }

        val eventType = eventTypeExtractor.extract(text)
        val orgs = orgNameExtractor.extract(text)
        val topOrg = orgs.firstOrNull()
        val people = peopleExtractor.extract(text)

        // ---- Tier 2: Score ----
        val scoring = confidenceScorer.score(
            dateConfidence = topDate?.confidence ?: 0f,
            eventConfidence = eventType.confidence,
            orgConfidence = topOrg?.confidence ?: 0f,
            peopleConfidence = if (people.isNotEmpty()) people.first().confidence else 0f
        )

        var usedNer = false
        var finalPeople = people.map { it.name }
        var finalOrg = topOrg?.name
        var finalDate = topDate

        // ---- Tier 3: NER fallback ----
        if (scoring.overallConfidence < 0.5f) {
            usedNer = true
            val nerResult = nerModelRunner.run(text)
            if (finalPeople.isEmpty()) finalPeople = nerResult.people
            if (finalOrg == null) finalOrg = nerResult.organizations.firstOrNull()
            // NER dates are raw strings; we re-run extraction on them if present
            if (finalDate == null && nerResult.dates.isNotEmpty()) {
                finalDate = nerResult.dates.firstOrNull()?.let {
                    dateExtractor.extract(it).maxByOrNull { d -> d.confidence }
                }
            }
        }

        // Derive title: first meaningful sentence fragment
        val title = text.lines().firstOrNull { it.trim().length > 5 }
            ?.take(80)?.trim()

        // Auto-tags from event type
        val tags = buildList {
            if (eventType.type != "OTHER") add(eventType.type.lowercase())
            finalOrg?.let { add(it.lowercase().replace(" ", "_")) }
        }

        return ExtractionResult(
            rawText = text,
            title = title,
            eventType = eventType.type.takeIf { it != "OTHER" },
            dateEpoch = finalDate?.epochMs,
            dateRaw = finalDate?.rawString,
            orgName = finalOrg,
            people = finalPeople.distinct(),
            tags = tags,
            confidence = scoring.overallConfidence,
            fieldConfidences = scoring.fieldConfidences,
            usedNer = usedNer
        )
    }
}
