package com.example.akashiconline.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.lastUsedDataStore: DataStore<Preferences> by preferencesDataStore(name = "last_used")
