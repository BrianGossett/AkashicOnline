package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Insert
    suspend fun insert(day: DayEntity)

    @Insert
    suspend fun insertAll(days: List<DayEntity>)

    @Query("SELECT * FROM days WHERE weekId = :weekId ORDER BY dayNumber ASC")
    fun getByWeek(weekId: String): Flow<List<DayEntity>>

    @Query("DELETE FROM days WHERE id = :id")
    suspend fun deleteById(id: String)
}
