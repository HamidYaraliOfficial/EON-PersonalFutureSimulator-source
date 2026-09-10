package com.eon.futuresimulator.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Security layer — any secret EON ever needs to hold (an optional Cloud AI API key, an
 * Export encryption passphrase hash) is stored via a Keystore-backed MasterKey, never
 * in plain SharedPreferences and never in the Room database itself.
 */
@Singleton
class KeystoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "eon_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun putSecret(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    fun getSecret(key: String): String? = encryptedPrefs.getString(key, null)

    fun removeSecret(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    companion object {
        const val KEY_CLOUD_AI_API_KEY = "cloud_ai_api_key"
        const val KEY_EXPORT_PASSPHRASE_HASH = "export_passphrase_hash"
    }
}
