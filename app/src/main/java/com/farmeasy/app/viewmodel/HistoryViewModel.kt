package com.farmeasy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.data.local.SensorReadingEntity
import com.farmeasy.app.data.repository.SensorRepository
import com.farmeasy.app.data.repository.SensorStatistics
import com.farmeasy.app.utils.Constants
import com.farmeasy.app.utils.getStartOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimeRange(val label: String, val hours: Int) {
    SIX_HOURS("6 Hours", 6),
    TWENTY_FOUR_HOURS("24 Hours", 24),
    SEVEN_DAYS("7 Days", 168),
    THIRTY_DAYS("30 Days", 720)
}

data class HistoryUiState(
    val readings: List<SensorReadingEntity> = emptyList(),
    val statistics: SensorStatistics = SensorStatistics(null, null, null, null, null, null),
    val selectedTimeRange: TimeRange = TimeRange.TWENTY_FOUR_HOURS,
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun selectTimeRange(range: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = range)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val range = _uiState.value.selectedTimeRange
            val fromTimestamp = System.currentTimeMillis() / 1000 - (range.hours * 3600L)

            // Observe readings
            sensorRepository.getReadingsSince(Constants.DEFAULT_NODE_ID, fromTimestamp).collect { readings ->
                val stats = sensorRepository.getStatistics(Constants.DEFAULT_NODE_ID, fromTimestamp)
                _uiState.value = _uiState.value.copy(
                    readings = readings,
                    statistics = stats,
                    isLoading = false
                )
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            sensorRepository.fetchFromCloud(
                Constants.DEFAULT_NODE_ID,
                when (_uiState.value.selectedTimeRange) {
                    TimeRange.SIX_HOURS -> "6h"
                    TimeRange.TWENTY_FOUR_HOURS -> "24h"
                    TimeRange.SEVEN_DAYS -> "7d"
                    TimeRange.THIRTY_DAYS -> "30d"
                }
            )
        }
    }
}
