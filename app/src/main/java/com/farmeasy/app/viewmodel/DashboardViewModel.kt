package com.farmeasy.app.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.bluetooth.BleConnectionState
import com.farmeasy.app.bluetooth.BleManager
import com.farmeasy.app.bluetooth.SensorPacket
import com.farmeasy.app.data.remote.YieldPredictionResponse
import com.farmeasy.app.data.repository.AlertRepository
import com.farmeasy.app.data.repository.FarmRepository
import com.farmeasy.app.data.repository.SensorRepository
import com.farmeasy.app.utils.Constants
import com.farmeasy.app.utils.getCropStage
import com.farmeasy.app.utils.getDaysSincePlanting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val sensorData: SensorPacket? = null,
    val connectionState: BleConnectionState = BleConnectionState.IDLE,
    val connectedDeviceName: String? = null,
    val lastUpdated: Long? = null,
    val cropStage: String = "Formative Phase",
    val daysSincePlanting: Int = 0,
    val yieldProjection: Float = 0f,
    val yieldTrend: String = "stable",
    val nextIrrigation: String = "Not scheduled",
    val unreadAlertCount: Int = 0,
    val isLoading: Boolean = true,
    val isOffline: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val farmRepository: FarmRepository,
    private val alertRepository: AlertRepository,
    private val bleManager: BleManager,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val unreadAlertCount = alertRepository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        observeBleState()
        observeSensorData()
        loadFarmData()
        loadDemoData()
    }

    private fun observeBleState() {
        viewModelScope.launch {
            bleManager.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }
        viewModelScope.launch {
            bleManager.connectedDevice.collect { device ->
                _uiState.value = _uiState.value.copy(
                    connectedDeviceName = device?.displayName
                )
            }
        }
    }

    private fun observeSensorData() {
        viewModelScope.launch {
            sensorRepository.sensorDataStream.collect { packet ->
                _uiState.value = _uiState.value.copy(
                    sensorData = packet,
                    lastUpdated = packet.timestamp,
                    isLoading = false
                )
                // Save to Room
                sensorRepository.saveSensorPacket(packet)
            }
        }

        // Also observe from Room for persisted data
        viewModelScope.launch {
            sensorRepository.getLatestReading(Constants.DEFAULT_NODE_ID).collect { entity ->
                if (entity != null && _uiState.value.sensorData == null) {
                    _uiState.value = _uiState.value.copy(
                        sensorData = SensorPacket(
                            nodeId = entity.nodeId,
                            soilMoisture = entity.soilMoisture,
                            soilTemperature = entity.soilTemperature,
                            ambientTemp = entity.ambientTemp,
                            humidity = entity.humidity,
                            rainfallMm = entity.rainfallMm,
                            valveStatus = entity.valveStatus,
                            batteryPct = entity.batteryPct,
                            timestamp = entity.timestamp
                        ),
                        lastUpdated = entity.timestamp,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadFarmData() {
        viewModelScope.launch {
            farmRepository.farmProfile.collect { profile ->
                _uiState.value = _uiState.value.copy(
                    cropStage = getCropStage(profile.plantingDate),
                    daysSincePlanting = getDaysSincePlanting(profile.plantingDate)
                )
            }
        }
    }

    private fun loadDemoData() {
        viewModelScope.launch {
            // Load demo sensor data if no BLE data available
            kotlinx.coroutines.delay(1500)
            if (_uiState.value.sensorData == null) {
                val demo = sensorRepository.getDemoSensorPacket()
                _uiState.value = _uiState.value.copy(
                    sensorData = demo,
                    lastUpdated = demo.timestamp,
                    yieldProjection = 68.5f,
                    yieldTrend = "up",
                    nextIrrigation = "Today, 10:30 PM",
                    isLoading = false,
                    isOffline = true
                )
            }

            // Load demo alerts
            alertRepository.fetchFromCloud()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            bleManager.requestManualRead()

            // Try cloud sync
            sensorRepository.syncToCloud()
            alertRepository.fetchFromCloud()

            kotlinx.coroutines.delay(1000)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
