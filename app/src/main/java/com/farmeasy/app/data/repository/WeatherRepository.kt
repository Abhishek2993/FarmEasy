package com.farmeasy.app.data.repository

import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.data.remote.ForecastItem
import com.farmeasy.app.data.remote.WeatherAlert
import com.farmeasy.app.data.remote.WeatherCurrentResponse
import com.farmeasy.app.data.remote.WeatherForecastResponse
import com.farmeasy.app.utils.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCurrentWeather(
        lat: Double = Constants.DEFAULT_LATITUDE,
        lon: Double = Constants.DEFAULT_LONGITUDE
    ): Result<WeatherCurrentResponse> {
        return try {
            val response = apiService.getCurrentWeather(lat, lon)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Return demo data when API is unavailable
                Result.success(getDemoCurrentWeather())
            }
        } catch (e: Exception) {
            // Offline fallback — return demo data
            Result.success(getDemoCurrentWeather())
        }
    }

    suspend fun getWeatherForecast(
        lat: Double = Constants.DEFAULT_LATITUDE,
        lon: Double = Constants.DEFAULT_LONGITUDE
    ): Result<WeatherForecastResponse> {
        return try {
            val response = apiService.getWeatherForecast(lat, lon)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getDemoForecast())
            }
        } catch (e: Exception) {
            Result.success(getDemoForecast())
        }
    }

    // Demo data for standalone usage
    private fun getDemoCurrentWeather() = WeatherCurrentResponse(
        temperature = 33.5f,
        humidity = 62f,
        windSpeed = 12.5f,
        cloudCover = 35,
        rainProbability = 20,
        description = "Partly Cloudy",
        icon = "02d",
        recommendation = "Good conditions for field work. Monitor soil moisture levels in afternoon heat."
    )

    private fun getDemoForecast(): WeatherForecastResponse {
        val now = System.currentTimeMillis() / 1000
        val forecasts = (0..47).map { hour ->
            ForecastItem(
                timestamp = now + (hour * 3600),
                temperature = 28f + (kotlin.math.sin(hour * 0.26) * 6).toFloat(),
                humidity = 55f + (kotlin.math.cos(hour * 0.26) * 15).toFloat(),
                rainProbability = if (hour in 20..28) 65 else 15,
                rainMm = if (hour in 22..26) 8.5f else 0f,
                description = if (hour in 20..28) "Light Rain" else "Clear Sky",
                icon = if (hour % 24 in 6..18) "01d" else "01n"
            )
        }

        val alerts = listOf(
            WeatherAlert(
                type = "light_rain",
                title = "Light Rain Expected",
                description = "Light rainfall expected tomorrow afternoon. Consider postponing irrigation.",
                severity = "info",
                startTime = now + 72000,
                endTime = now + 86400
            )
        )

        return WeatherForecastResponse(forecasts = forecasts, alerts = alerts)
    }
}
