package com.example.mindbox.query

import com.example.mindbox.nlp.extractor.DateExtractor
import com.example.mindbox.nlp.extractor.OrgNameExtractor
import javax.inject.Inject

/**
 * Parsed representation of a natural-language query.
 */
data class ParsedQuery(
    val raw: String,
    val intent: QueryIntent,
    val keywords: List<String> = emptyList(),
    val eventType: String? = null,
    val orgHint: String? = null,
    val dateFrom: Long? = null,
    val dateTo: Long? = null
)

enum class QueryIntent {
    DATE_QUERY,   // "when was my ..."
    PEOPLE_QUERY, // "who ..." / "did I meet ..."
    ORG_QUERY,    // "what happened at ..."
    EVENT_QUERY,  // "what events ..." / "did I have ..."
    NOTE_QUERY,   // "what did I note about ..."
    GENERAL       // fallback
}

/**
 * Parses a natural-language question into structured slots
 * using keyword heuristics.
 */
class QueryParser @Inject constructor(
    private val dateExtractor: DateExtractor,
    private val orgExtractor: OrgNameExtractor
) {

    fun parse(question: String): ParsedQuery {
        val lower = question.lowercase()

        val intent = when {
            lower.startsWith("when") || lower.contains("what date") ||
                    lower.contains("what time") -> QueryIntent.DATE_QUERY
            lower.startsWith("who") || lower.contains("who did i") ||
                    lower.contains("met with") || lower.contains("did i meet") -> QueryIntent.PEOPLE_QUERY
            lower.contains("at ") || lower.contains("what happened at") ||
                    lower.contains("interview at") || lower.contains("meeting at") -> QueryIntent.ORG_QUERY
            lower.contains("note") || lower.contains("wrote") ||
                    lower.contains("what did i write") -> QueryIntent.NOTE_QUERY
            lower.contains("interview") || lower.contains("meeting") ||
                    lower.contains("appointment") || lower.contains("event") -> QueryIntent.EVENT_QUERY
            else -> QueryIntent.GENERAL
        }

        val dates = dateExtractor.extract(question)
        val orgs = orgExtractor.extract(question)

        // Extract a window of search keywords by removing question words
        val stopWords = setOf(
            "when", "who", "what", "where", "why", "how", "did", "was", "is", "are",
            "i", "my", "me", "the", "a", "an", "at", "with", "for", "of", "in", "on",
            "interview", "meeting", "had", "have", "get", "got"
        )
        val keywords = question.lowercase().split(Regex("\\W+"))
            .filter { it.length > 2 && it !in stopWords }

        // Detect event type hint from known keywords
        val eventType = when {
            lower.contains("interview") -> "INTERVIEW"
            lower.contains("meeting") -> "MEETING"
            lower.contains("appointment") -> "APPOINTMENT"
            lower.contains("doctor") || lower.contains("medical") -> "MEDICAL"
            lower.contains("flight") || lower.contains("travel") -> "TRAVEL"
            else -> null
        }

        return ParsedQuery(
            raw = question,
            intent = intent,
            keywords = keywords,
            eventType = eventType,
            orgHint = orgs.firstOrNull()?.name,
            dateFrom = dates.firstOrNull()?.epochMs?.let { it - 24 * 3600 * 1000L },
            dateTo = dates.firstOrNull()?.epochMs?.let { it + 24 * 3600 * 1000L }
        )
    }
}
