package com.farmeasy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.bluetooth.BleManager
import com.farmeasy.app.data.local.IrrigationEventEntity
import com.farmeasy.app.data.remote.IrrigationScheduleResponse
import com.farmeasy.app.data.remote.IrrigationSlot
import com.farmeasy.app.data.remote.ZoneStatus
import com.farmeasy.app.data.repository.IrrigationRepository
import com.farmeasy.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IrrigationUiState(
    val zoneStatuses: List<ZoneStatus> = emptyList(),
    val todaySchedule: List<IrrigationSlot> = emptyList(),
    val weeklyWaterUsage: Float = 0f,
    val weeklyWaterTarget: Float = 6000f,
    val irrigationHistory: List<IrrigationEventEntity> = emptyList(),
    val smartMessage: String? = null,
    val isLoading: Boolean = true,
    val overrideInProgress: Boolean = false,
    val overrideResult: String? = null
)

@HiltViewModel
class IrrigationViewModel @Inject constructor(
    private val irrigationRepository: IrrigationRepository,
    private val bleManager: BleManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(IrrigationUiState())
    val uiState: StateFlow<IrrigationUiState> = _uiState.asStateFlow()

    init {
        loadSchedule()
        observeHistory()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = irrigationRepository.getSchedule()
            result.onSuccess { schedule ->
                _uiState.value = _uiState.value.copy(
                    zoneStatuses = schedule.currentStatus,
                    todaySchedule = schedule.schedule,
                    weeklyWaterUsage = schedule.weeklyWaterUsage,
                    weeklyWaterTarget = schedule.weeklyWaterTarget,
                    smartMessage = schedule.smartMessage,
                    isLoading = false
                )
            }
            result.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            irrigationRepository.getAllEvents().collect { events ->
                _uiState.value = _uiState.value.copy(irrigationHistory = events)
            }
        }
    }

    fun overrideIrrigation(zone: Int, action: String, durationMinutes: Int? = 30) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(overrideInProgress = true)

            // Send BLE command
            val open = action == "start"
            bleManager.sendValveCommand(zone, open)

            // Notify cloud
            val result = irrigationRepository.overrideIrrigation(
                farmId = Constants.DEFAULT_FARM_ID,
                zone = zone,
                action = action,
                durationMinutes = durationMinutes
            )

            result.onSuccess { message ->
                _uiState.value = _uiState.value.copy(
                    overrideInProgress = false,
                    overrideResult = message
                )
            }
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    overrideInProgress = false,
                    overrideResult = e.message
                )
            }

            // Refresh schedule
            loadSchedule()
        }
    }

    fun refresh() {
        loadSchedule()
    }

    fun clearOverrideResult() {
        _uiState.value = _uiState.value.copy(overrideResult = null)
    }
}
