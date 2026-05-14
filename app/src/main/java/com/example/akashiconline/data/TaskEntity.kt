package com.example.akashiconline.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val name: String,
    val details: String?,
    val dueDateEpochDay: Long?,
    val isCompleted: Boolean = false,
    val completedAt: Long?,
    val createdAt: Long,
)
