package com.farmeasy.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_readings")
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nodeId: Int,
    val soilMoisture: Float,
    val soilTemperature: Float,
    val ambientTemp: Float,
    val humidity: Float,
    val rainfallMm: Float,
    val valveStatus: Boolean,
    val batteryPct: Int,
    val timestamp: Long, // Unix timestamp from ESP32
    val receivedAt: Long = System.currentTimeMillis(), // When app received it
    val isSynced: Boolean = false // Whether synced to cloud
)
