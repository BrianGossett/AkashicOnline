package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workoutId")],
)
data class RoundEntity(
    @PrimaryKey val id: String,
    val workoutId: String,
    val order: Int,
    val name: String,
    val durationSeconds: Int?,
    val isRestRound: Boolean,
    val weightKg: Float?,
    val reps: Int?,
)
