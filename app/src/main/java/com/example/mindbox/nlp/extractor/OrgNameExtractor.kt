package com.example.mindbox.nlp.extractor

import java.util.regex.Pattern
import javax.inject.Inject

data class CandidateOrg(val name: String, val confidence: Float)

/**
 * Detects organization names using:
 * 1. Known-brand dictionary (high confidence)
 * 2. Prepositions before capitalized sequences ("at OpenAI", "with Google", "from Microsoft")
 * 3. Capitalized multi-word sequences (lower confidence fallback)
 */
class OrgNameExtractor @Inject constructor() {

    companion object {
        // Common well-known organizations and companies
        private val KNOWN_BRANDS = setOf(
            "Google", "Alphabet", "Microsoft", "Apple", "Amazon", "Meta", "Facebook",
            "Netflix", "Tesla", "OpenAI", "Anthropic", "DeepMind", "IBM", "Oracle",
            "Salesforce", "Adobe", "Nvidia", "Intel", "AMD", "Qualcomm",
            "Twitter", "X", "LinkedIn", "Spotify", "Uber", "Lyft", "Airbnb",
            "Samsung", "Sony", "LG", "Huawei", "Xiaomi", "Oppo",
            "Harvard", "MIT", "Stanford", "Oxford", "Cambridge", "IIT", "NIT",
            "WHO", "UN", "NASA", "ISRO", "ESA", "CIA", "FBI", "IRS",
            // Healthcare
            "Apollo", "Medanta", "Fortis", "AIIMS",
            // Finance
            "JPMorgan", "Goldman Sachs", "Morgan Stanley", "Citibank", "HSBC",
            "PayPal", "Stripe", "Visa", "Mastercard"
        )

        // Preposition triggers: "at <org>", "with <org>", "from <org>", "for <org>", "joined <org>"
        private val PREP_PATTERN = Pattern.compile(
            """(?:at|with|from|for|joined|hired by|interned at|working at|work at|employed by)\s+([A-Z][a-zA-Z0-9&.\s]{1,40})""",
            Pattern.CASE_INSENSITIVE
        )

        // Capitalized multi-word: 2–4 capitalized words
        private val CAP_SEQ_PATTERN = Pattern.compile(
            """(?<!\. )(?:[A-Z][a-z]+\s){1,3}[A-Z][a-z]+"""
        )

        private val EXCLUDE_WORDS = setOf(
            "I", "A", "The", "This", "That", "He", "She", "We", "They",
            "My", "Your", "His", "Her", "Our", "Their", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    fun extract(text: String): List<CandidateOrg> {
        val results = mutableListOf<CandidateOrg>()
        val seen = mutableSetOf<String>()

        // 1. Known brands
        for (brand in KNOWN_BRANDS) {
            if (text.contains(brand, ignoreCase = true) && seen.add(brand.lowercase())) {
                results += CandidateOrg(brand, 0.95f)
            }
        }

        // 2. Preposition triggers
        PREP_PATTERN.matcher(text).also { m ->
            while (m.find()) {
                val candidate = m.group(1)!!.trim().trimEnd('.', ',', ';')
                if (candidate.isNotBlank() && seen.add(candidate.lowercase())) {
                    results += CandidateOrg(candidate, 0.80f)
                }
            }
        }

        // 3. Capitalized sequences (fallback, lower confidence)
        if (results.isEmpty()) {
            CAP_SEQ_PATTERN.matcher(text).also { m ->
                while (m.find()) {
                    val candidate = m.group(0)!!.trim()
                    if (candidate !in EXCLUDE_WORDS && seen.add(candidate.lowercase())) {
                        results += CandidateOrg(candidate, 0.55f)
                    }
                }
            }
        }

        return results.sortedByDescending { it.confidence }
    }
}
