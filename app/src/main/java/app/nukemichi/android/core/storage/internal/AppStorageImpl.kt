package app.nukemichi.android.core.storage.internal

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import app.nukemichi.android.core.storage.AppStorage
import app.nukemichi.android.core.storage.SecureStorageUnreadableException
import app.nukemichi.android.core.storage.StorageDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AppStorageImpl @Inject constructor(
    @ApplicationContext context: Context,
) : AppStorage {
    private val plainPrefs = context.getSharedPreferences(PLAIN_PREFS, Context.MODE_PRIVATE)
    private val securePrefs = context.getSharedPreferences(SECURE_PREFS, Context.MODE_PRIVATE)

    override fun getString(domain: StorageDomain, key: String): String? =
        read(domain, scopedKey(domain, key))

    override fun putString(domain: StorageDomain, key: String, value: String) {
        write(domain, scopedKey(domain, key), value)
    }

    // Exact match, not String.toBoolean(): a case-insensitive read would accept "TRUE" and any
    // other near-miss a future writer produces, and these flags gate what the app unlocks.
    override fun getBoolean(domain: StorageDomain, key: String): Boolean =
        getString(domain, key) == TRUE_VALUE

    override fun putBoolean(domain: StorageDomain, key: String, value: Boolean) {
        putString(domain, key, if (value) TRUE_VALUE else FALSE_VALUE)
    }

    override fun remove(domain: StorageDomain, key: String) {
        removeStored(domain, scopedKey(domain, key))
    }

    private fun read(domain: StorageDomain, key: String): String? = when {
        !domain.encrypted -> plainPrefs.getString(key, null)
        else -> securePrefs.getString(key, null)?.let { payload ->
            runCatching { decrypt(payload) }.getOrElse { error ->
                Timber.e(error, "Cannot decrypt the stored value for %s", key)
                throw SecureStorageUnreadableException(key, error)
            }
        }
    }

    private fun write(domain: StorageDomain, key: String, value: String) {
        if (domain.encrypted) {
            securePrefs.edit { putString(key, encrypt(value)) }
        } else {
            plainPrefs.edit { putString(key, value) }
        }
    }

    private fun removeStored(domain: StorageDomain, key: String) {
        if (domain.encrypted) securePrefs.edit { remove(key) }
        else plainPrefs.edit { remove(key) }
    }

    private fun scopedKey(domain: StorageDomain, key: String) = "${domain.keyPrefix}.$key"

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return "${Base64.getEncoder().encodeToString(cipher.iv)}:${Base64.getEncoder().encodeToString(encrypted)}"
    }

    private fun decrypt(payload: String): String {
        val separator = payload.indexOf(':')
        require(separator in 1 until payload.lastIndex) { "Invalid encrypted payload format" }
        val iv = Base64.getDecoder().decode(payload.substring(0, separator))
        val encrypted = Base64.getDecoder().decode(payload.substring(separator + 1))
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .setKeySize(256)
                    .build()
            )
        }.generateKey()
    }

    private companion object {
        const val TRUE_VALUE = "true"
        const val FALSE_VALUE = "false"
        const val PLAIN_PREFS = "plain_storage"
        const val SECURE_PREFS = "secure_storage"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "app.nukemichi.core.storage.master"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
