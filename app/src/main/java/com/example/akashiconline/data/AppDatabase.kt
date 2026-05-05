package com.example.akashiconline.data

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase

// Placeholder — remove when the first real entity is added.
@Entity
internal data class Placeholder(@PrimaryKey val id: Int = 0)

@Database(entities = [Placeholder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
