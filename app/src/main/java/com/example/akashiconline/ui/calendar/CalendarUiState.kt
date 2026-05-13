package com.example.akashiconline.ui.calendar

import com.example.akashiconline.data.CalendarEventEntity
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val displayedMonth: YearMonth,
    val selectedDate: LocalDate,
    val eventsThisMonth: Map<LocalDate, List<CalendarEventEntity>>,
    val selectedDayEvents: List<CalendarEventEntity>,
)
