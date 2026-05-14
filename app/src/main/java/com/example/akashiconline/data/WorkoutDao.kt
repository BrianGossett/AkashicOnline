package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity)

    @Update
    suspend fun update(workout: WorkoutEntity)

    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM workouts ORDER BY lastUsedAt IS NULL ASC, lastUsedAt DESC")
    fun getAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: String): WorkoutEntity?

    @Query("""
        SELECT * FROM workouts WHERE scheduledDate IS NOT NULL
        ORDER BY scheduledDate ASC,
            CASE WHEN scheduledTimeMinutes IS NULL THEN 1 ELSE 0 END ASC,
            scheduledTimeMinutes ASC
    """)
    fun getScheduled(): Flow<List<WorkoutEntity>>

    @Query("""
        SELECT * FROM workouts
        WHERE scheduledDate IS NOT NULL
          AND scheduledDate / 86400000 = :dateEpochDay
        ORDER BY CASE WHEN scheduledTimeMinutes IS NULL THEN 1 ELSE 0 END ASC, scheduledTimeMinutes ASC
    """)
    suspend fun getScheduledForDay(dateEpochDay: Long): List<WorkoutEntity>
}
