package com.farmeasy.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // weather, irrigation, fertilizer, yield, system, market
    val title: String,
    val description: String,
    val iconType: String, // icon identifier for UI
    val severity: String, // info, warning, critical
    val actionLabel: String? = null,
    val actionRoute: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceId: String? = null // ID from cloud for deduplication
)
