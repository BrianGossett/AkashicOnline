package com.example.akashiconline.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `tasks` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `details` TEXT,
                `dueDateEpochDay` INTEGER,
                `isCompleted` INTEGER NOT NULL DEFAULT 0,
                `completedAt` INTEGER,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `workout_session_logs` (
                `id` TEXT NOT NULL,
                `workoutId` TEXT NOT NULL,
                `workoutName` TEXT NOT NULL,
                `completedAt` INTEGER NOT NULL,
                `totalElapsedSeconds` INTEGER NOT NULL,
                `roundsCompleted` INTEGER NOT NULL,
                `totalRounds` INTEGER NOT NULL,
                `wasCompleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `workouts` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `scheduledDate` INTEGER,
                `repeatRule` TEXT,
                `reminderMinutesBefore` INTEGER,
                `createdAt` INTEGER NOT NULL,
                `lastUsedAt` INTEGER,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `rounds` (
                `id` TEXT NOT NULL,
                `workoutId` TEXT NOT NULL,
                `order` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `durationSeconds` INTEGER,
                `isRestRound` INTEGER NOT NULL,
                `weightKg` REAL,
                `reps` INTEGER,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_rounds_workoutId` ON `rounds` (`workoutId`)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `calendar_events` (
                `id` TEXT NOT NULL,
                `dateEpochDay` INTEGER NOT NULL,
                `featureSource` TEXT NOT NULL,
                `sourceId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `subtitle` TEXT,
                `isCompleted` INTEGER NOT NULL,
                `isAllDay` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

object DatabaseProvider {
    @Volatile private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "akashic_database"
            )
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
            .build()
            .also { instance = it }
        }
}
