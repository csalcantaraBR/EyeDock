package com.eyedock.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.eyedock.app.domain.model.Auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecureCredentialStore(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "eyedock_credentials",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    suspend fun saveCredentials(deviceId: String, auth: Auth) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .putString("${deviceId}_username", auth.username)
            .putString("${deviceId}_password", auth.password)
            .apply()
    }
    
    suspend fun getCredentials(deviceId: String): Auth? = withContext(Dispatchers.IO) {
        val username = encryptedPrefs.getString("${deviceId}_username", null)
        val password = encryptedPrefs.getString("${deviceId}_password", null)
        
        if (username != null && password != null) {
            Auth(username, password)
        } else {
            null
        }
    }
    
    suspend fun removeCredentials(deviceId: String) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .remove("${deviceId}_username")
            .remove("${deviceId}_password")
            .apply()
    }
    
    suspend fun hasCredentials(deviceId: String): Boolean = withContext(Dispatchers.IO) {
        encryptedPrefs.contains("${deviceId}_username") && 
        encryptedPrefs.contains("${deviceId}_password")
    }
    
    suspend fun clearAllCredentials() = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().clear().apply()
    }
}
