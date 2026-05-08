package com.farmeasy.app.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val language: String = "English",
    val temperatureUnit: String = "°C",
    val areaUnit: String = "Acres",
    val autoReconnect: Boolean = true,
    val lastSyncTimestamp: Long = 0,
    val savedDeviceName: String? = null,
    val notifyWeather: Boolean = true,
    val notifyIrrigation: Boolean = true,
    val notifyFertilizer: Boolean = true,
    val notifyYield: Boolean = true,
    val notifySystem: Boolean = true,
    val notifyMarket: Boolean = true,
    // Alert thresholds
    val moistureLowThreshold: Float = Constants.MOISTURE_CRITICAL_LOW,
    val moistureHighThreshold: Float = Constants.MOISTURE_HIGH,
    val tempLowThreshold: Float = Constants.TEMP_COLD_STRESS,
    val tempHighThreshold: Float = Constants.TEMP_HEAT_STRESS,
    val rainfallThreshold: Float = Constants.RAINFALL_HEAVY_THRESHOLD
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // Keys
    private val languageKey = stringPreferencesKey(Constants.PREF_LANGUAGE)
    private val tempUnitKey = stringPreferencesKey(Constants.PREF_TEMPERATURE_UNIT)
    private val areaUnitKey = stringPreferencesKey(Constants.PREF_AREA_UNIT)
    private val autoReconnectKey = booleanPreferencesKey(Constants.PREF_AUTO_RECONNECT)
    private val lastSyncKey = longPreferencesKey(Constants.PREF_LAST_SYNC)
    private val savedDeviceKey = stringPreferencesKey(Constants.PREF_LAST_DEVICE_NAME)
    private val notifyWeatherKey = booleanPreferencesKey(Constants.PREF_NOTIFY_WEATHER)
    private val notifyIrrigationKey = booleanPreferencesKey(Constants.PREF_NOTIFY_IRRIGATION)
    private val notifyFertilizerKey = booleanPreferencesKey(Constants.PREF_NOTIFY_FERTILIZER)
    private val notifyYieldKey = booleanPreferencesKey(Constants.PREF_NOTIFY_YIELD)
    private val notifySystemKey = booleanPreferencesKey(Constants.PREF_NOTIFY_SYSTEM)
    private val notifyMarketKey = booleanPreferencesKey(Constants.PREF_NOTIFY_MARKET)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _uiState.value = SettingsUiState(
                language = prefs[languageKey] ?: "English",
                temperatureUnit = prefs[tempUnitKey] ?: "°C",
                areaUnit = prefs[areaUnitKey] ?: "Acres",
                autoReconnect = prefs[autoReconnectKey] ?: true,
                lastSyncTimestamp = prefs[lastSyncKey] ?: 0,
                savedDeviceName = prefs[savedDeviceKey],
                notifyWeather = prefs[notifyWeatherKey] ?: true,
                notifyIrrigation = prefs[notifyIrrigationKey] ?: true,
                notifyFertilizer = prefs[notifyFertilizerKey] ?: true,
                notifyYield = prefs[notifyYieldKey] ?: true,
                notifySystem = prefs[notifySystemKey] ?: true,
                notifyMarket = prefs[notifyMarketKey] ?: true
            )
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            dataStore.edit { it[languageKey] = language }
            _uiState.value = _uiState.value.copy(language = language)
        }
    }

    fun setTemperatureUnit(unit: String) {
        viewModelScope.launch {
            dataStore.edit { it[tempUnitKey] = unit }
            _uiState.value = _uiState.value.copy(temperatureUnit = unit)
        }
    }

    fun setAreaUnit(unit: String) {
        viewModelScope.launch {
            dataStore.edit { it[areaUnitKey] = unit }
            _uiState.value = _uiState.value.copy(areaUnit = unit)
        }
    }

    fun setAutoReconnect(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[autoReconnectKey] = enabled }
            _uiState.value = _uiState.value.copy(autoReconnect = enabled)
        }
    }

    fun setNotification(category: String, enabled: Boolean) {
        viewModelScope.launch {
            when (category) {
                "weather" -> {
                    dataStore.edit { it[notifyWeatherKey] = enabled }
                    _uiState.value = _uiState.value.copy(notifyWeather = enabled)
                }
                "irrigation" -> {
                    dataStore.edit { it[notifyIrrigationKey] = enabled }
                    _uiState.value = _uiState.value.copy(notifyIrrigation = enabled)
                }
                "fertilizer" -> {
                    dataStore.edit { it[notifyFertilizerKey] = enabled }
                    _uiState.value = _uiState.value.copy(notifyFertilizer = enabled)
                }
                "yield" -> {
                    dataStore.edit { it[notifyYieldKey] = enabled }
                    _uiState.value = _uiState.value.copy(notifyYield = enabled)
                }
                "system" -> {
                    dataStore.edit { it[notifySystemKey] = enabled }
                    _uiState.value = _uiState.value.copy(notifySystem = enabled)
                }
                "market" -> {
                    dataStore.edit { it[notifyMarketKey] = enabled }
                    _uiState.value = _uiState.value.copy(notifyMarket = enabled)
                }
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            dataStore.edit { it[lastSyncKey] = now }
            _uiState.value = _uiState.value.copy(lastSyncTimestamp = now)
        }
    }

    companion object {
        val LANGUAGES = listOf("English", "हिन्दी", "मराठी")
        val TEMP_UNITS = listOf("°C", "°F")
        val AREA_UNITS = listOf("Acres", "Hectares")
    }
}
