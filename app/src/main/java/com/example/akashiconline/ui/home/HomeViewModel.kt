package com.example.akashiconline.ui.home

import android.app.Application
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.AppDestinations
import com.example.akashiconline.data.DatabaseProvider
import com.example.akashiconline.data.lastUsedDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val calendarDao = db.calendarEventDao()
    private val today = LocalDate.now()
    private val todayEpochDay = today.toEpochDay()

    val state: StateFlow<HomeUiState> = combine(
        calendarDao.getPastDueEvents(todayEpochDay),
        calendarDao.getUndatedTaskEvents(),
        calendarDao.getEventsForDate(todayEpochDay),
        application.lastUsedDataStore.data,
    ) { pastDue, undated, todayEvents, prefs ->
        val recent = AppDestinations.entries
            .sortedByDescending { prefs[longPreferencesKey("last_used_${it.route}")] ?: 0L }
            .take(5)
        HomeUiState(
            recentDestinations = recent,
            pastDueEvents = pastDue,
            undatedTasks = undated,
            todayEvents = todayEvents,
            todayDate = today,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            recentDestinations = AppDestinations.entries.take(5),
            pastDueEvents = emptyList(),
            undatedTasks = emptyList(),
            todayEvents = emptyList(),
            todayDate = today,
        ),
    )
}
