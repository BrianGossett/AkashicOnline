package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "session_logs",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("dayId")],
)
data class SessionLogEntity(
    @PrimaryKey val id: String,
    val dayId: String?,
    val completedAt: Long,
    val totalElapsedSeconds: Int,
    val wasCompleted: Boolean,
)
