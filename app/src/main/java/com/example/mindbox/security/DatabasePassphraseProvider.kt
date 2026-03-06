package com.example.mindbox.security

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabasePassphraseProvider @Inject constructor(
    private val keystoreManager: KeystoreManager,
    private val encryptedPreferences: EncryptedPreferences
) {
    companion object {
        private const val PREF_KEY_IV = "db_passphrase_iv"
        private const val PREF_KEY_CIPHER = "db_passphrase_cipher"
        private const val PASSPHRASE_LENGTH = 32
        private const val GCM_TAG_LENGTH = 128
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    private var cachedPassphrase: ByteArray? = null

    @Synchronized
    fun getPassphrase(): ByteArray {
        cachedPassphrase?.let { return it }

        val existingIv = encryptedPreferences.getString(PREF_KEY_IV)
        val existingCipher = encryptedPreferences.getString(PREF_KEY_CIPHER)

        return if (existingIv != null && existingCipher != null) {
            decryptPassphrase(existingIv, existingCipher)
        } else {
            createAndStorePassphrase()
        }.also { cachedPassphrase = it }
    }

    private fun createAndStorePassphrase(): ByteArray {
        val rawPassphrase = java.security.SecureRandom().let { rng ->
            ByteArray(PASSPHRASE_LENGTH).also { rng.nextBytes(it) }
        }
        val secretKey = keystoreManager.getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(rawPassphrase)

        encryptedPreferences.putString(PREF_KEY_IV, Base64.encodeToString(cipher.iv, Base64.DEFAULT))
        encryptedPreferences.putString(PREF_KEY_CIPHER, Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
        return rawPassphrase
    }

    private fun decryptPassphrase(ivBase64: String, cipherBase64: String): ByteArray {
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val encryptedBytes = Base64.decode(cipherBase64, Base64.DEFAULT)
        val secretKey = keystoreManager.getOrCreateSecretKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encryptedBytes)
    }
}
