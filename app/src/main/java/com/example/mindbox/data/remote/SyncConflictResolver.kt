package com.example.mindbox.data.remote

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Resolves conflicts between local and remote records.
 * Strategy: last-write-wins by [timestamp].
 * If the device was offline for more than 24h, remote wins unconditionally.
 */
@Singleton
class SyncConflictResolver @Inject constructor() {

    companion object {
        private const val OFFLINE_THRESHOLD_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    /**
     * @param localTimestamp  timestamp of the local record
     * @param remoteTimestamp timestamp of the remote record
     * @param lastSyncTime    timestamp of the last successful sync
     * @return true if remote version should overwrite local
     */
    fun shouldUseRemote(
        localTimestamp: Long,
        remoteTimestamp: Long,
        lastSyncTime: Long
    ): Boolean {
        val now = System.currentTimeMillis()
        val offlineDuration = now - lastSyncTime

        // Force remote if device was offline for more than 24h
        if (offlineDuration > OFFLINE_THRESHOLD_MS) return true

        // Otherwise last-write-wins
        return remoteTimestamp > localTimestamp
    }

    /**
     * Merge strategy for tags — union of both sets.
     */
    fun mergeTags(localTags: List<String>, remoteTags: List<String>): List<String> =
        (localTags + remoteTags).distinct()
}
