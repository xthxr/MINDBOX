package com.example.mindbox.nlp.extractor

import java.util.regex.Pattern
import javax.inject.Inject

data class CandidatePerson(val name: String, val confidence: Float)

/**
 * Detects person names using:
 * 1. Title-prefixed names: "Dr. John Smith", "Mr. Alice Brown"
 * 2. "met with / spoke to / called <Name>" patterns
 * 3. Capitalized two-word pairs that look like first + last names
 */
class PeopleExtractor @Inject constructor() {

    companion object {
        private val TITLES = setOf(
            "Dr", "Dr.", "Mr", "Mr.", "Mrs", "Mrs.", "Ms", "Ms.",
            "Prof", "Prof.", "Sir", "Capt", "Lt", "Col"
        )

        // Title + Name
        private val TITLE_PATTERN = Pattern.compile(
            """(Dr\.?|Mr\.?|Mrs\.?|Ms\.?|Prof\.?|Sir|Capt\.?|Lt\.?|Col\.?)\s+([A-Z][a-z]+(?:\s+[A-Z][a-z]+)?)""",
            Pattern.CASE_INSENSITIVE
        )

        // Verb + Name: "met with Alice Johnson", "called Bob", "spoke to Sarah Mills"
        private val VERB_PATTERN = Pattern.compile(
            """(?:met with|spoke to|called|talked to|emailed|messaged|introduced by|joined by|alongside)\s+([A-Z][a-z]+(?:\s+[A-Z][a-z]+)?)"""
        )

        // Generic FirstName LastName (capitalized two-word sequence not in exclude list)
        private val NAME_PAIR = Pattern.compile(
            """(?<![.!?]\s)([A-Z][a-z]{2,}\s[A-Z][a-z]{2,})"""
        )

        private val EXCLUDE = setOf(
            "The App", "Last Month", "Next Week", "Last Week",
            "New York", "San Francisco", "Los Angeles"
        )
    }

    fun extract(text: String): List<CandidatePerson> {
        val results = mutableListOf<CandidatePerson>()
        val seen = mutableSetOf<String>()

        // Title + Name
        TITLE_PATTERN.matcher(text).also { m ->
            while (m.find()) {
                val name = "${m.group(1)!!} ${m.group(2)!!}".trim()
                if (seen.add(name.lowercase())) results += CandidatePerson(name, 0.90f)
            }
        }

        // Verb + Name
        VERB_PATTERN.matcher(text).also { m ->
            while (m.find()) {
                val name = m.group(1)!!.trim()
                if (seen.add(name.lowercase())) results += CandidatePerson(name, 0.85f)
            }
        }

        // Generic name pairs
        NAME_PAIR.matcher(text).also { m ->
            while (m.find()) {
                val name = m.group(1)!!.trim()
                if (name !in EXCLUDE && seen.add(name.lowercase())) {
                    results += CandidatePerson(name, 0.60f)
                }
            }
        }

        return results.sortedByDescending { it.confidence }
    }
}
