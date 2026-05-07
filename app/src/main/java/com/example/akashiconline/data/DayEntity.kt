package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "days",
    foreignKeys = [
        ForeignKey(
            entity = WeekEntity::class,
            parentColumns = ["id"],
            childColumns = ["weekId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("weekId")],
)
data class DayEntity(
    @PrimaryKey val id: String,
    val weekId: String,
    val dayNumber: Int,
    val type: String,
    val label: String,
)
