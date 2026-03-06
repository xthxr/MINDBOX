package com.example.mindbox.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "mindbox_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_FIREBASE_UID = "firebase_uid"
        const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    fun getLong(key: String, default: Long = 0L): Long = prefs.getLong(key, default)
    fun putLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()

    fun getString(key: String, default: String? = null): String? = prefs.getString(key, default)
    fun putString(key: String, value: String?) = prefs.edit().putString(key, value).apply()

    fun getBoolean(key: String, default: Boolean = false): Boolean = prefs.getBoolean(key, default)
    fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()

    fun remove(key: String) = prefs.edit().remove(key).apply()
    fun clear() = prefs.edit().clear().apply()

    // Convenience helpers
    fun isBiometricEnabled(): Boolean = getBoolean(KEY_BIOMETRIC_ENABLED, false)
    fun setBiometricEnabled(enabled: Boolean) = putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
    fun getFirebaseUid(): String? = getString(KEY_FIREBASE_UID)
    fun setFirebaseUid(uid: String?) = putString(KEY_FIREBASE_UID, uid)
    fun clearFirebaseUid() = remove(KEY_FIREBASE_UID)
    fun getLastSyncTimestamp(): Long = getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
    fun setLastSyncTimestamp(ts: Long) = putLong(KEY_LAST_SYNC_TIMESTAMP, ts)
}
