package com.repoint.splash.accountmanager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "settings")

class SpManager(private val context: Context) {

    private val USER_ID_KEY = stringPreferencesKey("user_id")

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

}