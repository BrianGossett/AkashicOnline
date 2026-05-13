package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rounds: List<RoundEntity>)

    @Query("DELETE FROM rounds WHERE workoutId = :workoutId")
    suspend fun deleteByWorkoutId(workoutId: String)

    @Query("SELECT * FROM rounds WHERE workoutId = :workoutId ORDER BY `order` ASC")
    fun getRoundsForWorkout(workoutId: String): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds ORDER BY workoutId, `order` ASC")
    fun getAllRounds(): Flow<List<RoundEntity>>
}
