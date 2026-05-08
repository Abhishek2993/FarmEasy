package com.farmeasy.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SensorReadingEntity::class,
        AlertEntity::class,
        IrrigationEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun alertDao(): AlertDao
    abstract fun irrigationDao(): IrrigationDao

    companion object {
        const val DATABASE_NAME = "farmeasy_database"
    }
}
