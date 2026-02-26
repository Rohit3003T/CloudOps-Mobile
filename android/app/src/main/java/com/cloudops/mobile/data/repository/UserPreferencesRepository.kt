package com.cloudops.mobile.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cloudops_prefs")

object PrefsKeys {
    val TOKEN = stringPreferencesKey("auth_token")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_ID = stringPreferencesKey("user_id")
}

class UserPreferencesRepository(private val context: Context) {

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[PrefsKeys.TOKEN] }
    val userNameFlow: Flow<String?> = context.dataStore.data.map { it[PrefsKeys.USER_NAME] }
    val userEmailFlow: Flow<String?> = context.dataStore.data.map { it[PrefsKeys.USER_EMAIL] }

    suspend fun saveAuthData(token: String, name: String, email: String, id: String) {
        context.dataStore.edit { prefs ->
            prefs[PrefsKeys.TOKEN] = token
            prefs[PrefsKeys.USER_NAME] = name
            prefs[PrefsKeys.USER_EMAIL] = email
            prefs[PrefsKeys.USER_ID] = id
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { it.clear() }
    }
}
