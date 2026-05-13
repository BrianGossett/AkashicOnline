package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: WorkoutSessionLogEntity)

    @Query("SELECT * FROM workout_session_logs WHERE workoutId = :workoutId ORDER BY completedAt DESC")
    fun getLogsForWorkout(workoutId: String): Flow<List<WorkoutSessionLogEntity>>

    @Query("SELECT * FROM workout_session_logs ORDER BY completedAt DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<WorkoutSessionLogEntity>>
}
