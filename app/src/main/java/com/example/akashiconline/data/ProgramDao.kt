package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Insert
    suspend fun insert(program: ProgramEntity)

    @Query("SELECT * FROM programs ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ProgramEntity>>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getById(id: String): ProgramEntity?

    @Query("DELETE FROM programs WHERE id = :id")
    suspend fun deleteById(id: String)
}
