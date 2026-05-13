package com.example.akashiconline.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Insert
    suspend fun insert(event: CalendarEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM calendar_events WHERE dateEpochDay = :dateEpochDay ORDER BY createdAt ASC")
    fun getEventsForDate(dateEpochDay: Long): Flow<List<CalendarEventEntity>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay
        ORDER BY dateEpochDay ASC, createdAt ASC
    """)
    fun getEventsForDateRange(startEpochDay: Long, endEpochDay: Long): Flow<List<CalendarEventEntity>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE isCompleted = 0 AND dateEpochDay < :todayEpochDay AND dateEpochDay >= 0
        ORDER BY dateEpochDay ASC
    """)
    fun getPastDueEvents(todayEpochDay: Long): Flow<List<CalendarEventEntity>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE featureSource = 'TASK' AND dateEpochDay = -1
        ORDER BY createdAt DESC
    """)
    fun getUndatedTaskEvents(): Flow<List<CalendarEventEntity>>

    @Query("""
        SELECT * FROM calendar_events
        WHERE isCompleted = 1
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    fun getRecentlyCompletedEvents(limit: Int): Flow<List<CalendarEventEntity>>
}
