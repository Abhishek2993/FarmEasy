package com.farmeasy.app.data.repository

import com.farmeasy.app.bluetooth.BleManager
import com.farmeasy.app.bluetooth.SensorPacket
import com.farmeasy.app.data.local.SensorReadingDao
import com.farmeasy.app.data.local.SensorReadingEntity
import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.data.remote.SensorDataResponse
import com.farmeasy.app.data.remote.SensorSyncRequest
import com.farmeasy.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor(
    private val sensorReadingDao: SensorReadingDao,
    private val apiService: ApiService,
    private val bleManager: BleManager
) {
    // BLE sensor data stream
    val sensorDataStream = bleManager.sensorData

    fun getLatestReading(nodeId: Int = Constants.DEFAULT_NODE_ID): Flow<SensorReadingEntity?> {
        return sensorReadingDao.getLatestReading(nodeId)
    }

    fun getReadingsSince(nodeId: Int, fromTimestamp: Long): Flow<List<SensorReadingEntity>> {
        return sensorReadingDao.getReadingsSince(nodeId, fromTimestamp)
    }

    suspend fun saveSensorPacket(packet: SensorPacket) {
        val entity = SensorReadingEntity(
            nodeId = packet.nodeId,
            soilMoisture = packet.soilMoisture,
            soilTemperature = packet.soilTemperature,
            ambientTemp = packet.ambientTemp,
            humidity = packet.humidity,
            rainfallMm = packet.rainfallMm,
            valveStatus = packet.valveStatus,
            batteryPct = packet.batteryPct,
            timestamp = packet.timestamp
        )
        sensorReadingDao.insert(entity)
    }

    suspend fun getStatistics(nodeId: Int, fromTimestamp: Long): SensorStatistics {
        return SensorStatistics(
            avgMoisture = sensorReadingDao.getAverageMoisture(nodeId, fromTimestamp),
            minMoisture = sensorReadingDao.getMinMoisture(nodeId, fromTimestamp),
            maxMoisture = sensorReadingDao.getMaxMoisture(nodeId, fromTimestamp),
            avgTemperature = sensorReadingDao.getAverageTemperature(nodeId, fromTimestamp),
            minTemperature = sensorReadingDao.getMinTemperature(nodeId, fromTimestamp),
            maxTemperature = sensorReadingDao.getMaxTemperature(nodeId, fromTimestamp)
        )
    }

    suspend fun syncToCloud(): Result<Int> {
        return try {
            val unsynced = sensorReadingDao.getUnsyncedReadings()
            if (unsynced.isEmpty()) return Result.success(0)

            val request = SensorSyncRequest(
                readings = unsynced.map { it.toApiModel() }
            )
            val response = apiService.syncSensorData(request)
            if (response.isSuccessful) {
                val syncedIds = unsynced.map { it.id }
                sensorReadingDao.markAsSynced(syncedIds)
                Result.success(unsynced.size)
            } else {
                Result.failure(Exception("Sync failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchFromCloud(nodeId: Int, range: String): Result<List<SensorReadingEntity>> {
        return try {
            val response = apiService.getSensorHistory(nodeId, range)
            if (response.isSuccessful) {
                val readings = response.body()?.readings?.map { it.toEntity() } ?: emptyList()
                sensorReadingDao.insertAll(readings)
                Result.success(readings)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cleanupOldData(retentionDays: Long = Constants.DATA_RETENTION_DAYS) {
        val cutoff = System.currentTimeMillis() / 1000 - (retentionDays * 24 * 60 * 60)
        sensorReadingDao.deleteOlderThan(cutoff)
    }

    // Demo data for standalone usage
    fun getDemoSensorPacket(): SensorPacket {
        return SensorPacket(
            nodeId = 1,
            soilMoisture = 45.2f,
            soilTemperature = 28.5f,
            ambientTemp = 32.1f,
            humidity = 65.0f,
            rainfallMm = 0.0f,
            valveStatus = false,
            batteryPct = 87,
            timestamp = System.currentTimeMillis() / 1000
        )
    }
}

data class SensorStatistics(
    val avgMoisture: Float?,
    val minMoisture: Float?,
    val maxMoisture: Float?,
    val avgTemperature: Float?,
    val minTemperature: Float?,
    val maxTemperature: Float?
)

// Extension functions for converting between data models
private fun SensorReadingEntity.toApiModel() = SensorDataResponse(
    nodeId = nodeId,
    soilMoisture = soilMoisture,
    soilTemperature = soilTemperature,
    ambientTemp = ambientTemp,
    humidity = humidity,
    rainfallMm = rainfallMm,
    valveStatus = valveStatus,
    batteryPct = batteryPct,
    timestamp = timestamp
)

private fun SensorDataResponse.toEntity() = SensorReadingEntity(
    nodeId = nodeId,
    soilMoisture = soilMoisture,
    soilTemperature = soilTemperature,
    ambientTemp = ambientTemp,
    humidity = humidity,
    rainfallMm = rainfallMm,
    valveStatus = valveStatus,
    batteryPct = batteryPct,
    timestamp = timestamp,
    isSynced = true
)
