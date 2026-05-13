package com.example.akashiconline.ui.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.akashiconline.data.CalendarEventEntity
import com.example.akashiconline.data.DatabaseProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val calendarDao = DatabaseProvider.getDatabase(application).calendarEventDao()

    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    private val monthEvents = _displayedMonth.flatMapLatest { month ->
        calendarDao.getEventsForDateRange(
            startEpochDay = month.atDay(1).toEpochDay(),
            endEpochDay = month.atEndOfMonth().toEpochDay(),
        )
    }

    val state: StateFlow<CalendarUiState> = combine(
        _displayedMonth,
        _selectedDate,
        monthEvents,
    ) { month, selected, events ->
        val grouped = events.groupBy { LocalDate.ofEpochDay(it.dateEpochDay) }
        CalendarUiState(
            displayedMonth = month,
            selectedDate = selected,
            eventsThisMonth = grouped,
            selectedDayEvents = grouped[selected] ?: emptyList(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(
            displayedMonth = YearMonth.now(),
            selectedDate = LocalDate.now(),
            eventsThisMonth = emptyMap(),
            selectedDayEvents = emptyList(),
        ),
    )

    fun previousMonth() = _displayedMonth.update { it.minusMonths(1) }
    fun nextMonth() = _displayedMonth.update { it.plusMonths(1) }
    fun selectDate(date: LocalDate) { _selectedDate.value = date }

    fun toggleCompleted(event: CalendarEventEntity) {
        viewModelScope.launch {
            calendarDao.upsert(event.copy(isCompleted = !event.isCompleted))
        }
    }
}
