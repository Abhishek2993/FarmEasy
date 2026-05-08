package com.farmeasy.app.data.repository

import com.farmeasy.app.data.local.AlertDao
import com.farmeasy.app.data.local.AlertEntity
import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepository @Inject constructor(
    private val alertDao: AlertDao,
    private val apiService: ApiService
) {
    fun getAllAlerts(): Flow<List<AlertEntity>> = alertDao.getAllAlerts()

    fun getAlertsByCategory(category: String): Flow<List<AlertEntity>> =
        alertDao.getAlertsByCategory(category)

    fun getUnreadAlerts(): Flow<List<AlertEntity>> = alertDao.getUnreadAlerts()

    fun getUnreadCount(): Flow<Int> = alertDao.getUnreadCount()

    suspend fun markAsRead(alertId: Long) = alertDao.markAsRead(alertId)

    suspend fun markAllAsRead() = alertDao.markAllAsRead()

    suspend fun fetchFromCloud(farmId: Int = Constants.DEFAULT_FARM_ID): Result<Int> {
        return try {
            val response = apiService.getAlerts(farmId)
            if (response.isSuccessful && response.body() != null) {
                val cloudAlerts = response.body()!!.alerts
                var newCount = 0
                cloudAlerts.forEach { item ->
                    if (!alertDao.existsBySourceId(item.id)) {
                        alertDao.insert(
                            AlertEntity(
                                category = item.category,
                                title = item.title,
                                description = item.description,
                                iconType = item.icon,
                                severity = item.severity,
                                actionLabel = item.actionLabel,
                                actionRoute = item.actionRoute,
                                timestamp = item.timestamp * 1000,
                                sourceId = item.id
                            )
                        )
                        newCount++
                    }
                }
                Result.success(newCount)
            } else {
                loadDemoAlerts()
                Result.success(0)
            }
        } catch (e: Exception) {
            loadDemoAlerts()
            Result.success(0)
        }
    }

    suspend fun addLocalAlert(
        category: String,
        title: String,
        description: String,
        severity: String = "info",
        iconType: String = "info"
    ) {
        alertDao.insert(
            AlertEntity(
                category = category,
                title = title,
                description = description,
                iconType = iconType,
                severity = severity
            )
        )
    }

    private suspend fun loadDemoAlerts() {
        val demoAlerts = listOf(
            AlertEntity(
                category = "weather",
                title = "Light Rain Expected Tomorrow",
                description = "20mm rainfall expected between 2 PM - 6 PM. Afternoon irrigation has been automatically skipped.",
                iconType = "weather_rain",
                severity = "info",
                timestamp = System.currentTimeMillis()
            ),
            AlertEntity(
                category = "irrigation",
                title = "Evening Irrigation Scheduled",
                description = "Zone 1 irrigation scheduled for 10:30 PM (30 min). Soil moisture at 38% — below optimal range.",
                iconType = "water_drop",
                severity = "warning",
                actionLabel = "View Schedule",
                actionRoute = "irrigation",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            AlertEntity(
                category = "yield",
                title = "Weekly Yield Update",
                description = "Yield projection increased by 2.1% to 68.5 tonnes/hectare. Grand growth phase moisture levels are optimal.",
                iconType = "trending_up",
                severity = "info",
                timestamp = System.currentTimeMillis() - 86400000
            ),
            AlertEntity(
                category = "fertilizer",
                title = "Potash Application Reminder",
                description = "Grand growth phase: Apply 60 kg/acre MOP (Muriate of Potash) for optimal stalk development. Best applied after irrigation.",
                iconType = "eco",
                severity = "info",
                actionLabel = "Learn More",
                timestamp = System.currentTimeMillis() - 172800000
            ),
            AlertEntity(
                category = "system",
                title = "Sensor Battery Low",
                description = "ESP32 Node #1 battery at 15%. Please replace or recharge batteries soon to avoid data gaps.",
                iconType = "battery_alert",
                severity = "warning",
                timestamp = System.currentTimeMillis() - 259200000
            ),
            AlertEntity(
                category = "market",
                title = "Sugar Price Update",
                description = "Sugarcane FRP increased to ₹3,150/tonne. Prices trending upward — consider harvest timing.",
                iconType = "attach_money",
                severity = "info",
                actionLabel = "View Prices",
                actionRoute = "market",
                timestamp = System.currentTimeMillis() - 345600000
            )
        )

        demoAlerts.forEach { alert ->
            val sourceId = "demo_${alert.category}_${alert.title.hashCode()}"
            if (!alertDao.existsBySourceId(sourceId)) {
                alertDao.insert(alert.copy(sourceId = sourceId))
            }
        }
    }
}
