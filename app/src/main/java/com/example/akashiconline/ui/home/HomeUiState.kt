package com.example.akashiconline.ui.home

import com.example.akashiconline.AppDestinations
import com.example.akashiconline.data.CalendarEventEntity
import java.time.LocalDate

data class HomeUiState(
    val recentDestinations: List<AppDestinations>,
    val pastDueEvents: List<CalendarEventEntity>,
    val undatedTasks: List<CalendarEventEntity>,
    val todayEvents: List<CalendarEventEntity>,
    val todayDate: LocalDate,
)
