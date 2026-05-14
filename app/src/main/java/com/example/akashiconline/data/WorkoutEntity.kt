package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey val id: String,
    val name: String,
    val scheduledDate: Long?,
    val scheduledTimeMinutes: Int?,
    val repeatRule: String?,
    val reminderMinutesBefore: Int?,
    val createdAt: Long,
    val lastUsedAt: Long?,
)
