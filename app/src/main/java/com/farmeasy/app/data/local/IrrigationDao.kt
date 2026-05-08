package com.farmeasy.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IrrigationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: IrrigationEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<IrrigationEventEntity>)

    @Query("SELECT * FROM irrigation_events ORDER BY startTime DESC")
    fun getAllEvents(): Flow<List<IrrigationEventEntity>>

    @Query("SELECT * FROM irrigation_events WHERE startTime >= :fromTimestamp ORDER BY startTime DESC")
    fun getEventsSince(fromTimestamp: Long): Flow<List<IrrigationEventEntity>>

    @Query("SELECT * FROM irrigation_events WHERE zone = :zone ORDER BY startTime DESC")
    fun getEventsByZone(zone: Int): Flow<List<IrrigationEventEntity>>

    @Query("SELECT SUM(waterVolumeLiters) FROM irrigation_events WHERE startTime >= :fromTimestamp")
    suspend fun getTotalWaterUsage(fromTimestamp: Long): Float?

    @Query("SELECT SUM(durationMinutes) FROM irrigation_events WHERE startTime >= :fromTimestamp")
    suspend fun getTotalDuration(fromTimestamp: Long): Int?

    @Query("SELECT * FROM irrigation_events WHERE startTime >= :todayStart AND startTime <= :todayEnd ORDER BY startTime ASC")
    fun getTodayEvents(todayStart: Long, todayEnd: Long): Flow<List<IrrigationEventEntity>>

    @Query("DELETE FROM irrigation_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}
