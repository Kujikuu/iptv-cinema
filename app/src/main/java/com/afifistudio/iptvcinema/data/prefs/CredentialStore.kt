package com.afifistudio.iptvcinema.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "iptv_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun savePassword(sourceId: Long, password: String) {
        prefs.edit().putString(passwordKey(sourceId), password).apply()
    }

    fun getPassword(sourceId: Long): String? = prefs.getString(passwordKey(sourceId), null)

    fun deletePassword(sourceId: Long) {
        prefs.edit().remove(passwordKey(sourceId)).apply()
    }

    private fun passwordKey(sourceId: Long) = "source_password_$sourceId"
}
