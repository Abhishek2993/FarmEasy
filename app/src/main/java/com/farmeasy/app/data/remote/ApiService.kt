package com.farmeasy.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // ---- Sensor Data ----

    @GET("sensor-data/latest")
    suspend fun getLatestSensorData(
        @Query("node_id") nodeId: Int
    ): Response<SensorDataResponse>

    @GET("sensor-data/history")
    suspend fun getSensorHistory(
        @Query("node_id") nodeId: Int,
        @Query("range") range: String // "6h", "24h", "7d", "30d"
    ): Response<SensorHistoryResponse>

    @POST("sensor-data/sync")
    suspend fun syncSensorData(
        @Body request: SensorSyncRequest
    ): Response<SensorSyncResponse>

    // ---- Yield Prediction ----

    @GET("yield/prediction")
    suspend fun getYieldPrediction(
        @Query("farm_id") farmId: Int
    ): Response<YieldPredictionResponse>

    // ---- Irrigation ----

    @GET("irrigation/schedule")
    suspend fun getIrrigationSchedule(
        @Query("farm_id") farmId: Int
    ): Response<IrrigationScheduleResponse>

    @POST("irrigation/override")
    suspend fun overrideIrrigation(
        @Body request: IrrigationOverrideRequest
    ): Response<IrrigationOverrideResponse>

    // ---- Weather ----

    @GET("weather/current")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): Response<WeatherCurrentResponse>

    @GET("weather/forecast")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): Response<WeatherForecastResponse>

    // ---- Alerts ----

    @GET("alerts")
    suspend fun getAlerts(
        @Query("farm_id") farmId: Int
    ): Response<AlertsResponse>

    // ---- Farm Profile ----

    @POST("farm/profile")
    suspend fun saveFarmProfile(
        @Body request: FarmProfileRequest
    ): Response<FarmProfileResponse>

    // ---- Market Prices ----

    @GET("market/prices")
    suspend fun getMarketPrices(
        @Query("region") region: String
    ): Response<MarketPriceResponse>
}
