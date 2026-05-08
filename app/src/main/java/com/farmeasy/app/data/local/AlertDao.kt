package com.farmeasy.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<AlertEntity>)

    @Update
    suspend fun update(alert: AlertEntity)

    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE category = :category ORDER BY timestamp DESC")
    fun getAlertsByCategory(category: String): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAsRead(alertId: Long)

    @Query("UPDATE alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM alerts WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM alerts WHERE sourceId = :sourceId)")
    suspend fun existsBySourceId(sourceId: String): Boolean
}
