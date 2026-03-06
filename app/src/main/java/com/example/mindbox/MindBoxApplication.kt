package com.example.mindbox

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MindBoxApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        // Load SQLCipher native library before any database access
        System.loadLibrary("sqlcipher")
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
