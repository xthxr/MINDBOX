package com.example.mindbox.domain.model

data class RawEntry(
    val id: Long = 0,
    val rawText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isProcessed: Boolean = false,
    val inputSource: String = "TEXT"
)

data class Event(
    val id: Long = 0,
    val rawEntryId: Long? = null,
    val title: String,
    val eventType: String = "OTHER",
    val date: Long? = null,
    val dateRaw: String? = null,
    val orgId: Long? = null,
    val tags: String = "[]",
    val notes: String? = null,
    val peopleIds: String = "[]",
    val timestamp: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class Note(
    val id: Long = 0,
    val rawEntryId: Long? = null,
    val content: String,
    val tags: String = "[]",
    val timestamp: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class Person(
    val id: Long = 0,
    val name: String,
    val aliases: String = "[]",
    val linkedEventIds: String = "[]",
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class Organization(
    val id: Long = 0,
    val name: String,
    val aliases: String = "[]",
    val orgType: String = "UNKNOWN",
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class EmbeddingRecord(
    val id: Long = 0,
    val entryId: Long,
    val entryType: String,
    val vector: FloatArray,
    val modelVersion: String = "mobilebert_v1",
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmbeddingRecord) return false
        return id == other.id && entryId == other.entryId
    }
    override fun hashCode(): Int = 31 * id.hashCode() + entryId.hashCode()
}
