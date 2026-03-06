package com.example.mindbox.nlp.extractor

import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

data class ExtractedDate(
    val epochMs: Long,
    val rawString: String,
    val confidence: Float
)

/**
 * Rule-based date extractor. Handles:
 * - ISO dates: 2025-02-17
 * - Verbose: 17 Feb 2025, Feb 17, 2025, February 17th 2025
 * - Relative: yesterday, last Monday, 3 weeks ago, next Friday
 * - Compact: 17/02/2025, 02/17/2025
 */
class DateExtractor @Inject constructor() {

    companion object {
        private val MONTH_MAP = mapOf(
            "january" to 0, "jan" to 0,
            "february" to 1, "feb" to 1,
            "march" to 2, "mar" to 2,
            "april" to 3, "apr" to 3,
            "may" to 4,
            "june" to 5, "jun" to 5,
            "july" to 6, "jul" to 6,
            "august" to 7, "aug" to 7,
            "september" to 8, "sep" to 8, "sept" to 8,
            "october" to 9, "oct" to 9,
            "november" to 10, "nov" to 10,
            "december" to 11, "dec" to 11
        )

        private val DAY_OF_WEEK_MAP = mapOf(
            "monday" to Calendar.MONDAY,
            "tuesday" to Calendar.TUESDAY,
            "wednesday" to Calendar.WEDNESDAY,
            "thursday" to Calendar.THURSDAY,
            "friday" to Calendar.FRIDAY,
            "saturday" to Calendar.SATURDAY,
            "sunday" to Calendar.SUNDAY
        )

        // ISO: 2025-02-17
        private val ISO_PATTERN = Pattern.compile(
            """(\d{4})-(\d{1,2})-(\d{1,2})"""
        )

        // Verbose: 17 Feb 2025 | Feb 17 2025 | February 17th, 2025
        private val VERBOSE_PATTERN = Pattern.compile(
            """(\d{1,2})(?:st|nd|rd|th)?\s+(january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sep|sept|oct|nov|dec)\s+(\d{4})""",
            Pattern.CASE_INSENSITIVE
        )
        private val VERBOSE_PATTERN2 = Pattern.compile(
            """(january|february|march|april|may|june|july|august|september|october|november|december|jan|feb|mar|apr|jun|jul|aug|sep|sept|oct|nov|dec)\s+(\d{1,2})(?:st|nd|rd|th)?[,\s]+(\d{4})""",
            Pattern.CASE_INSENSITIVE
        )

        // Compact: 17/02/2025 or 02/17/2025
        private val COMPACT_PATTERN = Pattern.compile(
            """(\d{1,2})/(\d{1,2})/(\d{4})"""
        )

        // Relative: yesterday, today, last Monday, 3 weeks ago, 2 days ago, next Friday
        private val YESTERDAY = Pattern.compile("""yesterday""", Pattern.CASE_INSENSITIVE)
        private val TODAY = Pattern.compile("""today""", Pattern.CASE_INSENSITIVE)
        private val LAST_WEEKDAY = Pattern.compile(
            """last\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)""",
            Pattern.CASE_INSENSITIVE
        )
        private val NEXT_WEEKDAY = Pattern.compile(
            """next\s+(monday|tuesday|wednesday|thursday|friday|saturday|sunday)""",
            Pattern.CASE_INSENSITIVE
        )
        private val N_UNITS_AGO = Pattern.compile(
            """(\d+)\s+(day|days|week|weeks|month|months)\s+ago""",
            Pattern.CASE_INSENSITIVE
        )
        private val LAST_N_UNITS = Pattern.compile(
            """last\s+(\d+)\s+(day|days|week|weeks|month|months)""",
            Pattern.CASE_INSENSITIVE
        )
    }

    fun extract(text: String): List<ExtractedDate> {
        val results = mutableListOf<ExtractedDate>()

        // ISO
        ISO_PATTERN.matcher(text).also { m ->
            while (m.find()) {
                val (year, month, day) = listOf(m.group(1)!!.toInt(), m.group(2)!!.toInt() - 1, m.group(3)!!.toInt())
                calToEpoch(year, month, day)?.let {
                    results += ExtractedDate(it, m.group(0)!!, 0.95f)
                }
            }
        }

        // Verbose dd Mon YYYY
        VERBOSE_PATTERN.matcher(text).also { m ->
            while (m.find()) {
                val day = m.group(1)!!.toInt()
                val month = MONTH_MAP[m.group(2)!!.lowercase()] ?: return@also
                val year = m.group(3)!!.toInt()
                calToEpoch(year, month, day)?.let {
                    results += ExtractedDate(it, m.group(0)!!, 0.95f)
                }
            }
        }

        // Verbose Mon dd YYYY
        VERBOSE_PATTERN2.matcher(text).also { m ->
            while (m.find()) {
                val month = MONTH_MAP[m.group(1)!!.lowercase()] ?: return@also
                val day = m.group(2)!!.toInt()
                val year = m.group(3)!!.toInt()
                calToEpoch(year, month, day)?.let {
                    results += ExtractedDate(it, m.group(0)!!, 0.95f)
                }
            }
        }

        // Compact
        if (results.isEmpty()) {
            COMPACT_PATTERN.matcher(text).also { m ->
                while (m.find()) {
                    val a = m.group(1)!!.toInt(); val b = m.group(2)!!.toInt(); val year = m.group(3)!!.toInt()
                    // Heuristic: if a > 12 it must be day/month
                    val (day, month) = if (a > 12) Pair(a, b - 1) else Pair(b, a - 1)
                    calToEpoch(year, month, day)?.let {
                        results += ExtractedDate(it, m.group(0)!!, 0.75f)
                    }
                }
            }
        }

        // Relative
        val now = Calendar.getInstance()
        if (YESTERDAY.matcher(text).find() && results.isEmpty()) {
            val cal = now.clone() as Calendar; cal.add(Calendar.DAY_OF_YEAR, -1)
            results += ExtractedDate(cal.timeInMillis, "yesterday", 0.90f)
        }
        if (TODAY.matcher(text).find() && results.isEmpty()) {
            results += ExtractedDate(now.timeInMillis, "today", 0.90f)
        }

        val lastWeekday = LAST_WEEKDAY.matcher(text)
        if (lastWeekday.find() && results.isEmpty()) {
            val target = DAY_OF_WEEK_MAP[lastWeekday.group(1)!!.lowercase()]!!
            val cal = now.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -1)
            while (cal.get(Calendar.DAY_OF_WEEK) != target) cal.add(Calendar.DAY_OF_YEAR, -1)
            results += ExtractedDate(cal.timeInMillis, lastWeekday.group(0)!!, 0.85f)
        }

        val nextWeekday = NEXT_WEEKDAY.matcher(text)
        if (nextWeekday.find() && results.isEmpty()) {
            val target = DAY_OF_WEEK_MAP[nextWeekday.group(1)!!.lowercase()]!!
            val cal = now.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, 1)
            while (cal.get(Calendar.DAY_OF_WEEK) != target) cal.add(Calendar.DAY_OF_YEAR, 1)
            results += ExtractedDate(cal.timeInMillis, nextWeekday.group(0)!!, 0.85f)
        }

        val nAgo = N_UNITS_AGO.matcher(text)
        if (nAgo.find() && results.isEmpty()) {
            val n = nAgo.group(1)!!.toInt()
            val unit = nAgo.group(2)!!.lowercase()
            val cal = now.clone() as Calendar
            when {
                unit.startsWith("day") -> cal.add(Calendar.DAY_OF_YEAR, -n)
                unit.startsWith("week") -> cal.add(Calendar.WEEK_OF_YEAR, -n)
                unit.startsWith("month") -> cal.add(Calendar.MONTH, -n)
            }
            results += ExtractedDate(cal.timeInMillis, nAgo.group(0)!!, 0.80f)
        }

        val lastN = LAST_N_UNITS.matcher(text)
        if (lastN.find() && results.isEmpty()) {
            val n = lastN.group(1)!!.toInt()
            val unit = lastN.group(2)!!.lowercase()
            val cal = now.clone() as Calendar
            when {
                unit.startsWith("day") -> cal.add(Calendar.DAY_OF_YEAR, -n)
                unit.startsWith("week") -> cal.add(Calendar.WEEK_OF_YEAR, -n)
                unit.startsWith("month") -> cal.add(Calendar.MONTH, -n)
            }
            results += ExtractedDate(cal.timeInMillis, lastN.group(0)!!, 0.80f)
        }

        return results
    }

    private fun calToEpoch(year: Int, month: Int, day: Int): Long? {
        if (month < 0 || month > 11 || day < 1 || day > 31 || year < 1900 || year > 2100) return null
        return Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
