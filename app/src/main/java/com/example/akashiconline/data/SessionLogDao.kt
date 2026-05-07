package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionLogDao {
    @Insert
    suspend fun insert(log: SessionLogEntity)

    @Query("""
        SELECT session_logs.* FROM session_logs
        INNER JOIN days ON session_logs.dayId = days.id
        INNER JOIN weeks ON days.weekId = weeks.id
        WHERE weeks.programId = :programId
        ORDER BY session_logs.completedAt DESC
    """)
    fun getLogsForProgram(programId: String): Flow<List<SessionLogEntity>>
}
