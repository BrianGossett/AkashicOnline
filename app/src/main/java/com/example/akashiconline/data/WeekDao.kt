package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekDao {
    @Insert
    suspend fun insert(week: WeekEntity)

    @Insert
    suspend fun insertAll(weeks: List<WeekEntity>)

    @Query("SELECT * FROM weeks WHERE programId = :programId ORDER BY weekNumber ASC")
    fun getByProgram(programId: String): Flow<List<WeekEntity>>

    @Query("SELECT * FROM weeks WHERE programId = :programId ORDER BY weekNumber ASC")
    suspend fun getByProgramOnce(programId: String): List<WeekEntity>

    @Query("DELETE FROM weeks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM weeks WHERE programId = :programId")
    suspend fun deleteByProgram(programId: String)
}
