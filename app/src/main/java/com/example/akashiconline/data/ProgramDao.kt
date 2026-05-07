package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramDao {
    @Insert
    suspend fun insert(program: ProgramEntity)

    @Update
    suspend fun update(program: ProgramEntity)

    @Query("SELECT * FROM programs ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ProgramEntity>>

    @Query("""
        SELECT programs.id, programs.name, programs.description, programs.createdAt,
               COUNT(weeks.id) AS weekCount
        FROM programs
        LEFT JOIN weeks ON weeks.programId = programs.id
        GROUP BY programs.id
        ORDER BY programs.createdAt DESC
    """)
    fun getAllWithWeekCount(): Flow<List<ProgramSummary>>

    @Query("SELECT * FROM programs WHERE id = :id")
    suspend fun getById(id: String): ProgramEntity?

    @Transaction
    @Query("SELECT * FROM programs WHERE id = :programId")
    fun getProgramDetail(programId: String): Flow<ProgramDetail?>

    @Query("DELETE FROM programs WHERE id = :id")
    suspend fun deleteById(id: String)
}
