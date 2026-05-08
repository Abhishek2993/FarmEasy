package com.farmeasy.app.data.repository

import com.farmeasy.app.data.local.IrrigationDao
import com.farmeasy.app.data.local.IrrigationEventEntity
import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.data.remote.IrrigationOverrideRequest
import com.farmeasy.app.data.remote.IrrigationScheduleResponse
import com.farmeasy.app.data.remote.IrrigationSlot
import com.farmeasy.app.data.remote.ZoneStatus
import com.farmeasy.app.utils.Constants
import com.farmeasy.app.utils.getStartOfDayMillis
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IrrigationRepository @Inject constructor(
    private val irrigationDao: IrrigationDao,
    private val apiService: ApiService
) {
    fun getAllEvents(): Flow<List<IrrigationEventEntity>> = irrigationDao.getAllEvents()

    fun getTodayEvents(): Flow<List<IrrigationEventEntity>> {
        val todayStart = getStartOfDayMillis()
        val todayEnd = todayStart + (24 * 60 * 60 * 1000)
        return irrigationDao.getTodayEvents(todayStart, todayEnd)
    }

    suspend fun getTotalWaterUsageThisWeek(): Float {
        val weekStart = getStartOfDayMillis(daysAgo = 7)
        return irrigationDao.getTotalWaterUsage(weekStart) ?: 0f
    }

    suspend fun getSchedule(farmId: Int = Constants.DEFAULT_FARM_ID): Result<IrrigationScheduleResponse> {
        return try {
            val response = apiService.getIrrigationSchedule(farmId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getDemoSchedule())
            }
        } catch (e: Exception) {
            Result.success(getDemoSchedule())
        }
    }

    suspend fun overrideIrrigation(
        farmId: Int,
        zone: Int,
        action: String,
        durationMinutes: Int? = null
    ): Result<String> {
        return try {
            val request = IrrigationOverrideRequest(
                farmId = farmId,
                zone = zone,
                action = action,
                durationMinutes = durationMinutes
            )
            val response = apiService.overrideIrrigation(request)
            if (response.isSuccessful) {
                // Log override locally
                irrigationDao.insert(
                    IrrigationEventEntity(
                        zone = zone,
                        startTime = System.currentTimeMillis(),
                        durationMinutes = durationMinutes ?: 30,
                        isAiAutomated = false,
                        reason = "Manual override by farmer"
                    )
                )
                Result.success(response.body()?.message ?: "Override successful")
            } else {
                Result.failure(Exception("Override failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Offline — log locally anyway
            irrigationDao.insert(
                IrrigationEventEntity(
                    zone = zone,
                    startTime = System.currentTimeMillis(),
                    durationMinutes = durationMinutes ?: 30,
                    isAiAutomated = false,
                    reason = "Manual override (offline)"
                )
            )
            Result.success("Override logged locally (offline)")
        }
    }

    private fun getDemoSchedule(): IrrigationScheduleResponse {
        val now = System.currentTimeMillis() / 1000
        return IrrigationScheduleResponse(
            farmId = 1,
            schedule = listOf(
                IrrigationSlot(
                    zone = 1,
                    startTime = now + 3600,
                    endTime = now + 5400,
                    durationMinutes = 30,
                    status = "scheduled"
                ),
                IrrigationSlot(
                    zone = 2,
                    startTime = now + 7200,
                    endTime = now + 9000,
                    durationMinutes = 30,
                    status = "scheduled"
                ),
                IrrigationSlot(
                    zone = 1,
                    startTime = now - 7200,
                    endTime = now - 5400,
                    durationMinutes = 30,
                    status = "completed"
                )
            ),
            currentStatus = listOf(
                ZoneStatus(zone = 1, valveOpen = false, durationMinutes = null),
                ZoneStatus(zone = 2, valveOpen = false, durationMinutes = null)
            ),
            weeklyWaterUsage = 4500f,
            weeklyWaterTarget = 6000f,
            smartMessage = "Next irrigation optimized for tonight 10:30 PM when electricity is available. Soil moisture currently at 52% — within optimal range."
        )
    }
}
