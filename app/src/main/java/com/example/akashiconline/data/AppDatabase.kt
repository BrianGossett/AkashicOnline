package com.example.akashiconline.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PresetEntity::class,
        ProgramEntity::class,
        WeekEntity::class,
        DayEntity::class,
        StepEntity::class,
        SessionLogEntity::class,
        CalendarEventEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao
    abstract fun programDao(): ProgramDao
    abstract fun weekDao(): WeekDao
    abstract fun dayDao(): DayDao
    abstract fun stepDao(): StepDao
    abstract fun sessionLogDao(): SessionLogDao
    abstract fun calendarEventDao(): CalendarEventDao
}
