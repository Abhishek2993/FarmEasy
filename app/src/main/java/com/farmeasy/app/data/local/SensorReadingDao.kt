package com.farmeasy.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: SensorReadingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<SensorReadingEntity>)

    @Query("SELECT * FROM sensor_readings WHERE nodeId = :nodeId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(nodeId: Int): Flow<SensorReadingEntity?>

    @Query("SELECT * FROM sensor_readings WHERE nodeId = :nodeId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReadingOnce(nodeId: Int): SensorReadingEntity?

    @Query("SELECT * FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    fun getReadingsSince(nodeId: Int, fromTimestamp: Long): Flow<List<SensorReadingEntity>>

    @Query("SELECT * FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp ORDER BY timestamp ASC")
    suspend fun getReadingsSinceOnce(nodeId: Int, fromTimestamp: Long): List<SensorReadingEntity>

    @Query("SELECT AVG(soilMoisture) FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp")
    suspend fun getAverageMoisture(nodeId: Int, fromTimestamp: Long): Float?

    @Query("SELECT MIN(soilMoisture) FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp")
    suspend fun getMinMoisture(nodeId: Int, fromTimestamp: Long): Float?

    @Query("SELECT MAX(soilMoisture) FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp")
    suspend fun getMaxMoisture(nodeId: Int, fromTimestamp: Long): Float?

    @Query("SELECT AVG(soilTemperature) FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp")
    suspend fun getAverageTemperature(nodeId: Int, fromTimestamp: Long): Float?

    @Query("SELECT MIN(soilTemperature) FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp")
    suspend fun getMinTemperature(nodeId: Int, fromTimestamp: Long): Float?

    @Query("SELECT MAX(soilTemperature) FROM sensor_readings WHERE nodeId = :nodeId AND timestamp >= :fromTimestamp")
    suspend fun getMaxTemperature(nodeId: Int, fromTimestamp: Long): Float?

    @Query("SELECT * FROM sensor_readings WHERE isSynced = 0 ORDER BY timestamp ASC LIMIT 100")
    suspend fun getUnsyncedReadings(): List<SensorReadingEntity>

    @Query("UPDATE sensor_readings SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM sensor_readings WHERE nodeId = :nodeId")
    suspend fun getReadingCount(nodeId: Int): Int

    @Query("DELETE FROM sensor_readings WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}
