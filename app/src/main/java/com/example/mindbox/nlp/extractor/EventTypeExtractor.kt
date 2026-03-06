package com.example.mindbox.nlp.extractor

import javax.inject.Inject

data class ExtractedEventType(val type: String, val confidence: Float)

/**
 * Keyword-dictionary based event type extractor.
 * Returns the highest-confidence match from a prioritised set of event categories.
 */
class EventTypeExtractor @Inject constructor() {

    companion object {
        val EVENT_TYPES = linkedMapOf(
            "INTERVIEW" to listOf(
                "interview", "job interview", "hiring", "recruiter", "technical round",
                "coding round", "hr round", "onsite", "screening call"
            ),
            "MEETING" to listOf(
                "meeting", "call", "catch up", "sync", "standup", "stand-up",
                "scrum", "retrospective", "discussion", "conference", "webinar"
            ),
            "APPOINTMENT" to listOf(
                "appointment", "scheduled", "book", "booked", "slot", "session"
            ),
            "DEADLINE" to listOf(
                "deadline", "due", "submit", "submission", "last date", "expires", "expiry"
            ),
            "MEDICAL" to listOf(
                "doctor", "hospital", "clinic", "checkup", "check-up", "prescription",
                "diagnosis", "surgery", "therapy", "dentist", "physiotherapy"
            ),
            "TRAVEL" to listOf(
                "flight", "travel", "bus", "train", "trip", "journey", "departure",
                "arrival", "hotel", "accommodation", "boarding"
            ),
            "PURCHASE" to listOf(
                "bought", "purchase", "order", "delivered", "receipt", "paid", "invoice",
                "bought", "subscription", "renewal"
            ),
            "EDUCATION" to listOf(
                "class", "lecture", "exam", "test", "assignment", "course", "study",
                "semester", "university", "college", "school", "graduation"
            ),
            "SOCIAL" to listOf(
                "party", "dinner", "lunch", "birthday", "wedding", "celebration",
                "hangout", "met with", "visited"
            ),
            "OTHER" to emptyList()
        )
    }

    fun extract(text: String): ExtractedEventType {
        val lower = text.lowercase()
        var bestType = "OTHER"
        var bestScore = 0f

        for ((type, keywords) in EVENT_TYPES) {
            if (type == "OTHER") continue
            val matchCount = keywords.count { lower.contains(it) }
            if (matchCount > 0) {
                val score = (matchCount.toFloat() / keywords.size).coerceIn(0.3f, 1.0f)
                if (score > bestScore) {
                    bestScore = score
                    bestType = type
                }
            }
        }

        return ExtractedEventType(
            type = bestType,
            confidence = if (bestType == "OTHER") 0.2f else bestScore.coerceAtLeast(0.6f)
        )
    }
}
