package com.example.akashiconline.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
            .addMigrations(MIGRATION_4_5)
            .build()
            .also { instance = it }
        }
}
