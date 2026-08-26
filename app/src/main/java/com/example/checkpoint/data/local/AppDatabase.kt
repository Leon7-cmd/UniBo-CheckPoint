package com.example.checkpoint.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.checkpoint.data.entity.AchievementEntity
import com.example.checkpoint.data.entity.GameEntity
import com.example.checkpoint.data.local.dao.AchievementDao
import com.example.checkpoint.data.local.dao.GameDao

/**
 * Main Room database instance providing access to local game entities and cached achievements.
 */
@Database(
    entities = [
        GameEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        private const val DATABASE_NAME = "checkpoint_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}