package com.example.mindbox.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PERIODIC_SYNC_WORK = "mindbox_periodic_sync"
        private const val ONE_TIME_SYNC_WORK = "mindbox_one_time_sync"
        private const val SYNC_INTERVAL_HOURS = 6L
    }

    private val workManager = WorkManager.getInstance(context)

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Schedule a periodic sync every 6 hours, network-required. */
    fun schedulePeriodic() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Trigger an immediate one-time sync. */
    fun scheduleImmediate() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            ONE_TIME_SYNC_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelAll() {
        workManager.cancelAllWork()
    }

    fun getSyncState() = workManager.getWorkInfosForUniqueWorkLiveData(ONE_TIME_SYNC_WORK)
}
