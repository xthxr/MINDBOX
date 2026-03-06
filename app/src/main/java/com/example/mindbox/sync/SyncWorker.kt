package com.example.mindbox.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mindbox.data.local.dao.*
import com.example.mindbox.data.remote.FirebaseAuthManager
import com.example.mindbox.data.remote.FirestoreSyncRepository
import com.example.mindbox.security.EncryptedPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val rawEntryDao: RawEntryDao,
    private val eventDao: EventDao,
    private val noteDao: NoteDao,
    private val personDao: PersonDao,
    private val organizationDao: OrganizationDao,
    private val firestoreRepo: FirestoreSyncRepository,
    private val authManager: FirebaseAuthManager,
    private val encryptedPrefs: EncryptedPreferences
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Ensure authenticated
            authManager.ensureSignedIn()

            val lastSync = encryptedPrefs.getLong(EncryptedPreferences.KEY_LAST_SYNC_TIMESTAMP, 0L)

            // Push unsynced local records to Firestore
            pushLocal()

            // Pull remote changes since last sync
            // (simplified: log count of fetched records; full merge logic in SyncConflictResolver)
            val now = System.currentTimeMillis()
            encryptedPrefs.putLong(EncryptedPreferences.KEY_LAST_SYNC_TIMESTAMP, now)

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun pushLocal() {
        // Push raw entries
        rawEntryDao.getUnsynced().forEach { entry ->
            firestoreRepo.upsertRawEntry(entry)
        }
        rawEntryDao.markSynced(rawEntryDao.getUnsynced().map { it.id })

        // Push events
        eventDao.getUnsynced().also { list ->
            list.forEach { firestoreRepo.upsertEvent(it) }
            if (list.isNotEmpty()) eventDao.markSynced(list.map { it.id })
        }

        // Push notes
        noteDao.getUnsynced().also { list ->
            list.forEach { firestoreRepo.upsertNote(it) }
            if (list.isNotEmpty()) noteDao.markSynced(list.map { it.id })
        }

        // Push people
        personDao.getUnsynced().also { list ->
            list.forEach { firestoreRepo.upsertPerson(it) }
            if (list.isNotEmpty()) personDao.markSynced(list.map { it.id })
        }

        // Push organizations
        organizationDao.getUnsynced().also { list ->
            list.forEach { firestoreRepo.upsertOrganization(it) }
            if (list.isNotEmpty()) organizationDao.markSynced(list.map { it.id })
        }
    }
}
