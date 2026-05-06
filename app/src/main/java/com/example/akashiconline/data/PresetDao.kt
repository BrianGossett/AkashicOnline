package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Insert
    suspend fun insert(preset: PresetEntity)

    @Query("SELECT * FROM presets ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PresetEntity>>

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deleteById(id: String)
}
