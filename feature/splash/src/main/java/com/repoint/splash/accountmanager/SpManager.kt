package com.repoint.splash.accountmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "settings")

class SpManager(private val context: Context) {

    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val WALLET_ID_KEY = stringPreferencesKey("wallet_id")

    suspend fun setUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    fun getUserIdFlow() : Flow<String?>{
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    suspend fun setActiveWallet(walletId : String){
        context.dataStore.edit { prefrences ->
            prefrences[WALLET_ID_KEY] = walletId
        }
    }

    fun getActiveWalletId() : Flow<String?>{
        return context.dataStore.data.map { prefrences ->
            prefrences[WALLET_ID_KEY]
        }
    }

    // ✅ StateFlow version — always available, real-time observable
    val activeWalletIdFlow: StateFlow<String?> by lazy {
        context.dataStore.data
            .map { prefs -> prefs[WALLET_ID_KEY] }
            .stateIn(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                started = SharingStarted.Eagerly,
                initialValue = null
            )
    }

    suspend fun saveTheme(isDark : Boolean){
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("theme_dark")] = isDark
        }
    }


    fun getTheme() : Flow<Boolean>{
        return context.dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("theme_dark")] ?: false

        }
    }

}