package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("dayId")],
)
data class StepEntity(
    @PrimaryKey val id: String,
    val dayId: String,
    val order: Int,
    val name: String,
    val durationSeconds: Int,
    val isRestStep: Boolean,
)
