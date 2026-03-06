package com.example.mindbox.domain.usecase

import com.example.mindbox.sync.SyncScheduler
import javax.inject.Inject

class SyncUseCase @Inject constructor(
    private val syncScheduler: SyncScheduler
) {
    operator fun invoke() = syncScheduler.scheduleImmediate()
}
