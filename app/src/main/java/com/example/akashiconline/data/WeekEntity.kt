package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weeks",
    foreignKeys = [
        ForeignKey(
            entity = ProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("programId")],
)
data class WeekEntity(
    @PrimaryKey val id: String,
    val programId: String,
    val weekNumber: Int,
    val label: String,
    val phase: String,
    val phaseDescription: String,
)
