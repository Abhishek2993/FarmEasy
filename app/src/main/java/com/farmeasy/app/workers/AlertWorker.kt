package com.farmeasy.app.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.farmeasy.app.data.local.SensorReadingDao
import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.data.remote.SensorDataResponse
import com.farmeasy.app.data.remote.SensorSyncRequest
import com.farmeasy.app.data.repository.AlertRepository
import com.farmeasy.app.utils.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AlertWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val sensorReadingDao: SensorReadingDao,
    private val apiService: ApiService,
    private val alertRepository: AlertRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Sync unsynced sensor data to cloud
            syncSensorData()

            // 2. Fetch new alerts from cloud
            fetchAlerts()

            // 3. Check local sensor thresholds
            checkThresholds()

            // 4. Cleanup old data
            cleanupOldData()

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun syncSensorData() {
        try {
            val unsynced = sensorReadingDao.getUnsyncedReadings()
            if (unsynced.isEmpty()) return

            val request = SensorSyncRequest(
                readings = unsynced.map { reading ->
                    SensorDataResponse(
                        nodeId = reading.nodeId,
                        soilMoisture = reading.soilMoisture,
                        soilTemperature = reading.soilTemperature,
                        ambientTemp = reading.ambientTemp,
                        humidity = reading.humidity,
                        rainfallMm = reading.rainfallMm,
                        valveStatus = reading.valveStatus,
                        batteryPct = reading.batteryPct,
                        timestamp = reading.timestamp
                    )
                }
            )
            val response = apiService.syncSensorData(request)
            if (response.isSuccessful) {
                sensorReadingDao.markAsSynced(unsynced.map { it.id })
            }
        } catch (_: Exception) {
            // Offline — will retry next cycle
        }
    }

    private suspend fun fetchAlerts() {
        try {
            alertRepository.fetchFromCloud(Constants.DEFAULT_FARM_ID)
        } catch (_: Exception) {
            // Offline
        }
    }

    private suspend fun checkThresholds() {
        val latestReading = sensorReadingDao.getLatestReadingOnce(Constants.DEFAULT_NODE_ID) ?: return

        // Check soil moisture
        if (latestReading.soilMoisture < Constants.MOISTURE_CRITICAL_LOW) {
            sendNotification(
                title = "⚠️ Critical: Soil Moisture Low",
                message = "Soil moisture at ${latestReading.soilMoisture}% — below critical threshold. Immediate irrigation recommended.",
                channelId = Constants.CHANNEL_ALERTS
            )
            alertRepository.addLocalAlert(
                category = "irrigation",
                title = "Critical: Soil Moisture Low",
                description = "Soil moisture at ${latestReading.soilMoisture}% — below critical threshold of ${Constants.MOISTURE_CRITICAL_LOW}%. Immediate irrigation recommended.",
                severity = "critical",
                iconType = "water_drop"
            )
        }

        // Check temperature
        if (latestReading.soilTemperature > Constants.TEMP_HEAT_STRESS) {
            sendNotification(
                title = "🌡️ Heat Stress Alert",
                message = "Soil temperature at ${latestReading.soilTemperature}°C — above heat stress threshold.",
                channelId = Constants.CHANNEL_ALERTS
            )
        }

        // Check battery
        if (latestReading.batteryPct <= 15) {
            sendNotification(
                title = "🔋 Sensor Battery Low",
                message = "ESP32 battery at ${latestReading.batteryPct}%. Replace or recharge soon.",
                channelId = Constants.CHANNEL_SYSTEM
            )
        }
    }

    private suspend fun cleanupOldData() {
        val cutoffTimestamp = System.currentTimeMillis() / 1000 - (Constants.DATA_RETENTION_DAYS * 24 * 60 * 60)
        sensorReadingDao.deleteOlderThan(cutoffTimestamp)
    }

    private fun sendNotification(title: String, message: String, channelId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                when (channelId) {
                    Constants.CHANNEL_ALERTS -> "Farm Alerts"
                    Constants.CHANNEL_IRRIGATION -> "Irrigation Updates"
                    Constants.CHANNEL_SYSTEM -> "System Health"
                    else -> "Notifications"
                },
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val WORK_NAME = "farm_alert_worker"
    }
}
