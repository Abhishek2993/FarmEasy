package com.farmeasy.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.data.remote.FarmProfileRequest
import com.farmeasy.app.data.remote.MarketPriceResponse
import com.farmeasy.app.data.remote.PricePoint
import com.farmeasy.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FarmRepository @Inject constructor(
    private val apiService: ApiService,
    private val dataStore: DataStore<Preferences>
) {
    // DataStore Keys
    private val farmNameKey = stringPreferencesKey("farm_name")
    private val farmLatKey = doublePreferencesKey(Constants.PREF_FARM_LAT)
    private val farmLonKey = doublePreferencesKey(Constants.PREF_FARM_LON)
    private val farmAreaKey = stringPreferencesKey("farm_area")
    private val varietyKey = stringPreferencesKey("sugarcane_variety")
    private val plantingSeasonKey = stringPreferencesKey("planting_season")
    private val plantingDateKey = longPreferencesKey(Constants.PREF_PLANTING_DATE)
    private val ratoonCycleKey = intPreferencesKey("ratoon_cycle")
    private val irrigationZonesKey = intPreferencesKey("irrigation_zones")
    private val waterSourceKey = stringPreferencesKey("water_source")
    private val electricityWindowKey = stringPreferencesKey("electricity_window")
    private val farmIdKey = intPreferencesKey(Constants.PREF_FARM_ID)

    data class FarmProfile(
        val farmName: String = "",
        val latitude: Double = Constants.DEFAULT_LATITUDE,
        val longitude: Double = Constants.DEFAULT_LONGITUDE,
        val areaAcres: Float = 3f,
        val sugarcaneVariety: String = "Co-86032",
        val plantingSeason: String = "Suru (Jan-Feb)",
        val plantingDate: Long = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000), // 90 days ago
        val ratoonCycle: Int = 0,
        val irrigationZones: Int = 2,
        val waterSource: String = "Borewell",
        val electricityWindow: String = "10 PM - 6 AM",
        val farmId: Int = Constants.DEFAULT_FARM_ID
    )

    val farmProfile: Flow<FarmProfile> = dataStore.data.map { prefs ->
        FarmProfile(
            farmName = prefs[farmNameKey] ?: "My Farm",
            latitude = prefs[farmLatKey] ?: Constants.DEFAULT_LATITUDE,
            longitude = prefs[farmLonKey] ?: Constants.DEFAULT_LONGITUDE,
            areaAcres = prefs[farmAreaKey]?.toFloatOrNull() ?: 3f,
            sugarcaneVariety = prefs[varietyKey] ?: "Co-86032",
            plantingSeason = prefs[plantingSeasonKey] ?: "Suru (Jan-Feb)",
            plantingDate = prefs[plantingDateKey] ?: (System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)),
            ratoonCycle = prefs[ratoonCycleKey] ?: 0,
            irrigationZones = prefs[irrigationZonesKey] ?: 2,
            waterSource = prefs[waterSourceKey] ?: "Borewell",
            electricityWindow = prefs[electricityWindowKey] ?: "10 PM - 6 AM",
            farmId = prefs[farmIdKey] ?: Constants.DEFAULT_FARM_ID
        )
    }

    suspend fun saveFarmProfile(profile: FarmProfile) {
        dataStore.edit { prefs ->
            prefs[farmNameKey] = profile.farmName
            prefs[farmLatKey] = profile.latitude
            prefs[farmLonKey] = profile.longitude
            prefs[farmAreaKey] = profile.areaAcres.toString()
            prefs[varietyKey] = profile.sugarcaneVariety
            prefs[plantingSeasonKey] = profile.plantingSeason
            prefs[plantingDateKey] = profile.plantingDate
            prefs[ratoonCycleKey] = profile.ratoonCycle
            prefs[irrigationZonesKey] = profile.irrigationZones
            prefs[waterSourceKey] = profile.waterSource
            prefs[electricityWindowKey] = profile.electricityWindow
        }

        // Sync to cloud
        try {
            val request = FarmProfileRequest(
                farmName = profile.farmName,
                latitude = profile.latitude,
                longitude = profile.longitude,
                areaAcres = profile.areaAcres,
                sugarcaneVariety = profile.sugarcaneVariety,
                plantingSeason = profile.plantingSeason,
                plantingDate = profile.plantingDate.toString(),
                ratoonCycle = profile.ratoonCycle,
                irrigationZones = profile.irrigationZones,
                waterSource = profile.waterSource,
                electricityWindow = profile.electricityWindow
            )
            val response = apiService.saveFarmProfile(request)
            if (response.isSuccessful) {
                response.body()?.farmId?.let { id ->
                    dataStore.edit { it[farmIdKey] = id }
                }
            }
        } catch (_: Exception) {
            // Offline mode — profile saved locally
        }
    }

    suspend fun getMarketPrices(region: String = Constants.DEFAULT_REGION): Result<MarketPriceResponse> {
        return try {
            val response = apiService.getMarketPrices(region)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getDemoMarketPrices())
            }
        } catch (e: Exception) {
            Result.success(getDemoMarketPrices())
        }
    }

    private fun getDemoMarketPrices() = MarketPriceResponse(
        region = "Pune",
        currentPrice = 3150f,
        priceTrend = (0..29).map { day ->
            PricePoint(
                date = "2026-${if ((28 - day) / 30 + 3 < 10) "0" else ""}${(28 - day) / 30 + 3}-${String.format("%02d", (28 - day) % 30 + 1)}",
                price = 3000f + (kotlin.math.sin(day * 0.2) * 150).toFloat()
            )
        },
        recommendation = "Prices are trending upward. Current maturity level suggests harvesting in 2-3 weeks would optimize returns.",
        lastUpdated = System.currentTimeMillis() / 1000
    )
}
