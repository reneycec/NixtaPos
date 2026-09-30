package com.example.data.repository

import android.content.Context
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "terminal_config")

@Singleton
class TerminalConfigManager @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val TENANT_ID = stringPreferencesKey("tenant_id")
        val SUCURSAL_ID = stringPreferencesKey("sucursal_id")
        val TERMINAL_ID = stringPreferencesKey("terminal_id")
        val LAST_SYNC = longPreferencesKey("last_sync")
        val IS_SYNCING = booleanPreferencesKey("is_syncing")
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        val CURRENT_USER_EMAIL = stringPreferencesKey("current_user_email")
        val JWT_TOKEN = stringPreferencesKey("jwt_token")
        val FOLIO_PREFIX = stringPreferencesKey("folio_prefix")
    }

    val apiBaseUrl: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            preferences[PreferencesKeys.API_BASE_URL] ?: "https://nixta.kuminova.com/api/v1/"
        }

    val currentUserId: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_USER_ID] }

    val currentUserEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.CURRENT_USER_EMAIL] }

    val jwtToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.JWT_TOKEN] }

    val folioPrefix: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.FOLIO_PREFIX] ?: "POS" }

    val tenantId: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            preferences[PreferencesKeys.TENANT_ID] ?: "TENANT-NIXTA-01" // Default temporal
        }

    val sucursalId: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            preferences[PreferencesKeys.SUCURSAL_ID] ?: "SUCURSAL-CENTRO" // Default temporal
        }

    val terminalId: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            val stored = preferences[PreferencesKeys.TERMINAL_ID]
            if (!stored.isNullOrBlank()) stored else "POS-TABLET-01"
        }

    val lastSync: Flow<Long> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC] ?: 0L
        }

    val isSyncing: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_SYNCING] ?: false
        }

    suspend fun updateTerminalId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.TERMINAL_ID] = id }
    }

    suspend fun updateTenantId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.TENANT_ID] = id }
    }

    suspend fun updateSucursalId(id: String) {
        context.dataStore.edit { it[PreferencesKeys.SUCURSAL_ID] = id }
    }

    suspend fun updateLastSync(timestamp: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_SYNC] = timestamp }
    }

    suspend fun updateSyncingStatus(syncing: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_SYNCING] = syncing }
    }

    suspend fun updateApiBaseUrl(url: String) {
        context.dataStore.edit { it[PreferencesKeys.API_BASE_URL] = url }
    }

    suspend fun updateCurrentUserId(userId: String?) {
        context.dataStore.edit { 
            if (userId == null) it.remove(PreferencesKeys.CURRENT_USER_ID)
            else it[PreferencesKeys.CURRENT_USER_ID] = userId 
        }
    }

    suspend fun updateCurrentUserEmail(email: String?) {
        context.dataStore.edit {
            if (email == null) it.remove(PreferencesKeys.CURRENT_USER_EMAIL)
            else it[PreferencesKeys.CURRENT_USER_EMAIL] = email
        }
    }

    suspend fun updateJwtToken(token: String?) {
        context.dataStore.edit {
            if (token == null) it.remove(PreferencesKeys.JWT_TOKEN)
            else it[PreferencesKeys.JWT_TOKEN] = token
        }
    }

    suspend fun updateFolioPrefix(prefix: String) {
        context.dataStore.edit { it[PreferencesKeys.FOLIO_PREFIX] = prefix }
    }
}
