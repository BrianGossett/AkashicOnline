package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Insert
    suspend fun insert(step: StepEntity)

    @Insert
    suspend fun insertAll(steps: List<StepEntity>)

    @Query("SELECT * FROM steps WHERE dayId = :dayId ORDER BY `order` ASC")
    fun getByDay(dayId: String): Flow<List<StepEntity>>

    @Query("SELECT * FROM steps WHERE dayId = :dayId ORDER BY `order` ASC")
    suspend fun getByDayOnce(dayId: String): List<StepEntity>

    @Query("DELETE FROM steps WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM steps WHERE dayId = :dayId")
    suspend fun deleteByDay(dayId: String)
}
