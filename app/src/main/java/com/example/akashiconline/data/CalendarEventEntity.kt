package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val dateEpochDay: Long,
    val featureSource: String,
    val sourceId: String,
    val title: String,
    val subtitle: String?,
    val isCompleted: Boolean = false,
    val isAllDay: Boolean = true,
    val timeMinutes: Int? = null,
    val createdAt: Long,
)
