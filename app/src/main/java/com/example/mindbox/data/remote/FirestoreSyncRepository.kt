package com.example.mindbox.data.remote

import com.example.mindbox.data.local.entity.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSyncRepository @Inject constructor(
    private val authManager: FirebaseAuthManager
) {
    private val db = FirebaseFirestore.getInstance()

    private fun userCollection(collection: String) =
        db.collection("users").document(authManager.currentUserId ?: "anon")
            .collection(collection)

    // ---- Raw Entries ----
    suspend fun upsertRawEntry(entity: RawEntryEntity) {
        userCollection("raw_entries").document(entity.id.toString())
            .set(entity.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchRawEntriesSince(since: Long): List<Map<String, Any>> {
        return userCollection("raw_entries")
            .whereGreaterThan("timestamp", since)
            .get().await()
            .documents.mapNotNull { it.data }
    }

    // ---- Events ----
    suspend fun upsertEvent(entity: EventEntity) {
        userCollection("events").document(entity.id.toString())
            .set(entity.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchEventsSince(since: Long): List<Map<String, Any>> {
        return userCollection("events")
            .whereGreaterThan("timestamp", since)
            .get().await()
            .documents.mapNotNull { it.data }
    }

    // ---- Notes ----
    suspend fun upsertNote(entity: NoteEntity) {
        userCollection("notes").document(entity.id.toString())
            .set(entity.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchNotesSince(since: Long): List<Map<String, Any>> {
        return userCollection("notes")
            .whereGreaterThan("timestamp", since)
            .get().await()
            .documents.mapNotNull { it.data }
    }

    // ---- People ----
    suspend fun upsertPerson(entity: PersonEntity) {
        userCollection("people").document(entity.id.toString())
            .set(entity.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchPeopleSince(since: Long): List<Map<String, Any>> {
        return userCollection("people")
            .whereGreaterThan("timestamp", since)
            .get().await()
            .documents.mapNotNull { it.data }
    }

    // ---- Organizations ----
    suspend fun upsertOrganization(entity: OrganizationEntity) {
        userCollection("organizations").document(entity.id.toString())
            .set(entity.toMap(), SetOptions.merge()).await()
    }

    suspend fun fetchOrganizationsSince(since: Long): List<Map<String, Any>> {
        return userCollection("organizations")
            .whereGreaterThan("timestamp", since)
            .get().await()
            .documents.mapNotNull { it.data }
    }

    // ---- Delete ----
    suspend fun deleteRemoteEntry(collection: String, id: Long) {
        userCollection(collection).document(id.toString()).delete().await()
    }
}

// Extension maps for serialization
private fun RawEntryEntity.toMap() = mapOf(
    "id" to id, "rawText" to rawText, "timestamp" to timestamp,
    "isProcessed" to isProcessed, "inputSource" to inputSource
)

private fun EventEntity.toMap() = mapOf(
    "id" to id, "rawEntryId" to rawEntryId, "title" to title,
    "eventType" to eventType, "date" to date, "dateRaw" to dateRaw,
    "orgId" to orgId, "tags" to tags, "notes" to notes,
    "peopleIds" to peopleIds, "timestamp" to timestamp, "lastModified" to lastModified
)

private fun NoteEntity.toMap() = mapOf(
    "id" to id, "rawEntryId" to rawEntryId, "content" to content,
    "tags" to tags, "timestamp" to timestamp, "lastModified" to lastModified
)

private fun PersonEntity.toMap() = mapOf(
    "id" to id, "name" to name, "aliases" to aliases,
    "linkedEventIds" to linkedEventIds, "timestamp" to timestamp
)

private fun OrganizationEntity.toMap() = mapOf(
    "id" to id, "name" to name, "aliases" to aliases,
    "orgType" to orgType, "timestamp" to timestamp
)
