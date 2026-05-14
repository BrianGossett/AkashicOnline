package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        SELECT * FROM tasks
        ORDER BY
            isCompleted ASC,
            CASE WHEN dueDateEpochDay IS NULL THEN 1 ELSE 0 END ASC,
            dueDateEpochDay ASC,
            CASE WHEN dueTimeMinutes IS NULL THEN 1 ELSE 0 END ASC,
            dueTimeMinutes ASC,
            createdAt ASC
    """)
    fun getAll(): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
          AND dueDateEpochDay IS NOT NULL
          AND dueDateEpochDay >= :todayEpochDay
        ORDER BY dueDateEpochDay ASC,
            CASE WHEN dueTimeMinutes IS NULL THEN 1 ELSE 0 END ASC,
            dueTimeMinutes ASC
    """)
    fun getUpcoming(todayEpochDay: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
          AND dueDateEpochDay = :dateEpochDay
        ORDER BY CASE WHEN dueTimeMinutes IS NULL THEN 1 ELSE 0 END ASC, dueTimeMinutes ASC
    """)
    suspend fun getTasksForDay(dateEpochDay: Long): List<TaskEntity>

    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
          AND dueDateEpochDay IS NOT NULL
          AND dueDateEpochDay < :todayEpochDay
        ORDER BY dueDateEpochDay ASC
    """)
    fun getOverdue(todayEpochDay: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE isCompleted = 0
          AND dueDateEpochDay IS NULL
        ORDER BY createdAt ASC
    """)
    fun getUndated(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getById(id: String): Flow<TaskEntity?>
}
