package com.farmeasy.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "irrigation_events")
data class IrrigationEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val zone: Int,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Int,
    val waterVolumeLiters: Float? = null,
    val isAiAutomated: Boolean = true, // true = AI scheduled, false = farmer override
    val reason: String? = null, // e.g., "Soil moisture below threshold"
    val timestamp: Long = System.currentTimeMillis()
)
