package com.farmeasy.app.data.remote

import com.google.gson.annotations.SerializedName

// ---- Sensor Data ----

data class SensorDataResponse(
    @SerializedName("node_id") val nodeId: Int,
    @SerializedName("soil_moisture") val soilMoisture: Float,
    @SerializedName("soil_temp") val soilTemperature: Float,
    @SerializedName("ambient_temp") val ambientTemp: Float,
    @SerializedName("humidity") val humidity: Float,
    @SerializedName("rainfall_mm") val rainfallMm: Float,
    @SerializedName("valve_status") val valveStatus: Boolean,
    @SerializedName("battery_pct") val batteryPct: Int,
    @SerializedName("timestamp") val timestamp: Long
)

data class SensorHistoryResponse(
    @SerializedName("node_id") val nodeId: Int,
    @SerializedName("readings") val readings: List<SensorDataResponse>,
    @SerializedName("range") val range: String
)

data class SensorSyncRequest(
    @SerializedName("readings") val readings: List<SensorDataResponse>
)

data class SensorSyncResponse(
    @SerializedName("synced_count") val syncedCount: Int,
    @SerializedName("status") val status: String
)

// ---- Yield Prediction ----

data class YieldPredictionResponse(
    @SerializedName("farm_id") val farmId: Int,
    @SerializedName("current_yield") val currentYield: Float, // tonnes/hectare
    @SerializedName("previous_season_yield") val previousSeasonYield: Float?,
    @SerializedName("trend") val trend: String, // "up", "down", "stable"
    @SerializedName("trend_pct") val trendPct: Float,
    @SerializedName("weekly_projections") val weeklyProjections: List<WeeklyYield>,
    @SerializedName("key_factors") val keyFactors: List<YieldFactor>,
    @SerializedName("ai_insight") val aiInsight: String,
    @SerializedName("last_updated") val lastUpdated: Long
)

data class WeeklyYield(
    @SerializedName("week") val week: Int,
    @SerializedName("yield_estimate") val yieldEstimate: Float,
    @SerializedName("timestamp") val timestamp: Long
)

data class YieldFactor(
    @SerializedName("name") val name: String,
    @SerializedName("impact") val impact: String, // "positive", "negative", "neutral"
    @SerializedName("value") val value: String,
    @SerializedName("description") val description: String
)

// ---- Irrigation ----

data class IrrigationScheduleResponse(
    @SerializedName("farm_id") val farmId: Int,
    @SerializedName("schedule") val schedule: List<IrrigationSlot>,
    @SerializedName("current_status") val currentStatus: List<ZoneStatus>,
    @SerializedName("weekly_water_usage") val weeklyWaterUsage: Float,
    @SerializedName("weekly_water_target") val weeklyWaterTarget: Float,
    @SerializedName("smart_message") val smartMessage: String?
)

data class IrrigationSlot(
    @SerializedName("zone") val zone: Int,
    @SerializedName("start_time") val startTime: Long,
    @SerializedName("end_time") val endTime: Long,
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("status") val status: String // "scheduled", "in_progress", "completed", "skipped"
)

data class ZoneStatus(
    @SerializedName("zone") val zone: Int,
    @SerializedName("valve_open") val valveOpen: Boolean,
    @SerializedName("duration_minutes") val durationMinutes: Int?
)

data class IrrigationOverrideRequest(
    @SerializedName("farm_id") val farmId: Int,
    @SerializedName("zone") val zone: Int,
    @SerializedName("action") val action: String, // "start" or "stop"
    @SerializedName("duration_minutes") val durationMinutes: Int? = null
)

data class IrrigationOverrideResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// ---- Weather ----

data class WeatherCurrentResponse(
    @SerializedName("temperature") val temperature: Float,
    @SerializedName("humidity") val humidity: Float,
    @SerializedName("wind_speed") val windSpeed: Float,
    @SerializedName("cloud_cover") val cloudCover: Int,
    @SerializedName("rain_probability") val rainProbability: Int,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("recommendation") val recommendation: String?
)

data class WeatherForecastResponse(
    @SerializedName("forecasts") val forecasts: List<ForecastItem>,
    @SerializedName("alerts") val alerts: List<WeatherAlert>
)

data class ForecastItem(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("temperature") val temperature: Float,
    @SerializedName("humidity") val humidity: Float,
    @SerializedName("rain_probability") val rainProbability: Int,
    @SerializedName("rain_mm") val rainMm: Float,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WeatherAlert(
    @SerializedName("type") val type: String, // "heavy_rain", "heatwave", "frost"
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("start_time") val startTime: Long,
    @SerializedName("end_time") val endTime: Long
)

// ---- Alerts ----

data class AlertsResponse(
    @SerializedName("alerts") val alerts: List<AlertItem>
)

data class AlertItem(
    @SerializedName("id") val id: String,
    @SerializedName("category") val category: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("severity") val severity: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("action_label") val actionLabel: String?,
    @SerializedName("action_route") val actionRoute: String?,
    @SerializedName("timestamp") val timestamp: Long
)

// ---- Farm Profile ----

data class FarmProfileRequest(
    @SerializedName("farm_name") val farmName: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("area_acres") val areaAcres: Float,
    @SerializedName("sugarcane_variety") val sugarcaneVariety: String,
    @SerializedName("planting_season") val plantingSeason: String,
    @SerializedName("planting_date") val plantingDate: String,
    @SerializedName("ratoon_cycle") val ratoonCycle: Int,
    @SerializedName("irrigation_zones") val irrigationZones: Int,
    @SerializedName("water_source") val waterSource: String,
    @SerializedName("electricity_window") val electricityWindow: String
)

data class FarmProfileResponse(
    @SerializedName("farm_id") val farmId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

// ---- Market Prices ----

data class MarketPriceResponse(
    @SerializedName("region") val region: String,
    @SerializedName("current_price") val currentPrice: Float, // per tonne
    @SerializedName("price_trend") val priceTrend: List<PricePoint>,
    @SerializedName("recommendation") val recommendation: String?,
    @SerializedName("last_updated") val lastUpdated: Long
)

data class PricePoint(
    @SerializedName("date") val date: String,
    @SerializedName("price") val price: Float
)
