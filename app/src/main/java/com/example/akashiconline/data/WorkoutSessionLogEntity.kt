package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_session_logs")
data class WorkoutSessionLogEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val workoutName: String,
    val completedAt: Long,
    val totalElapsedSeconds: Int,
    val roundsCompleted: Int,
    val totalRounds: Int,
    val wasCompleted: Boolean,
)
