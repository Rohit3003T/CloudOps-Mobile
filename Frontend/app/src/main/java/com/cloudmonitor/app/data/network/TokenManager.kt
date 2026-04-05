package com.cloudmonitor.app.data.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.tokenDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "cm_token_store")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN    = stringPreferencesKey("jwt_token")
        private val KEY_USERNAME = stringPreferencesKey("username")
    }

    val tokenFlow: Flow<String?> = context.tokenDataStore.data
        .map { it[KEY_TOKEN] }

    val usernameFlow: Flow<String?> = context.tokenDataStore.data
        .map { it[KEY_USERNAME] }

    suspend fun saveToken(token: String, username: String) {
        context.tokenDataStore.edit { prefs ->
            prefs[KEY_TOKEN]    = token
            prefs[KEY_USERNAME] = username
        }
    }

    suspend fun clearToken() {
        context.tokenDataStore.edit { it.clear() }
    }

    suspend fun getToken(): String? =
        tokenFlow.firstOrNull()

    fun bearerToken(token: String) = "Bearer $token"
}
