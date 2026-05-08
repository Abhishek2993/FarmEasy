package com.farmeasy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.data.remote.WeeklyYield
import com.farmeasy.app.data.remote.YieldFactor
import com.farmeasy.app.data.remote.YieldPredictionResponse
import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class YieldUiState(
    val currentYield: Float = 0f,
    val previousSeasonYield: Float? = null,
    val trend: String = "stable",
    val trendPct: Float = 0f,
    val weeklyProjections: List<WeeklyYield> = emptyList(),
    val keyFactors: List<YieldFactor> = emptyList(),
    val aiInsight: String = "",
    val lastUpdated: Long = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class YieldViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(YieldUiState())
    val uiState: StateFlow<YieldUiState> = _uiState.asStateFlow()

    init {
        loadYieldPrediction()
    }

    private fun loadYieldPrediction() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getYieldPrediction(Constants.DEFAULT_FARM_ID)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    _uiState.value = YieldUiState(
                        currentYield = data.currentYield,
                        previousSeasonYield = data.previousSeasonYield,
                        trend = data.trend,
                        trendPct = data.trendPct,
                        weeklyProjections = data.weeklyProjections,
                        keyFactors = data.keyFactors,
                        aiInsight = data.aiInsight,
                        lastUpdated = data.lastUpdated,
                        isLoading = false
                    )
                } else {
                    loadDemoData()
                }
            } catch (e: Exception) {
                loadDemoData()
            }
        }
    }

    private fun loadDemoData() {
        val now = System.currentTimeMillis() / 1000
        _uiState.value = YieldUiState(
            currentYield = 68.5f,
            previousSeasonYield = 62.3f,
            trend = "up",
            trendPct = 2.1f,
            weeklyProjections = (0..15).map { week ->
                WeeklyYield(
                    week = week + 1,
                    yieldEstimate = 45f + (week * 1.8f) + (kotlin.math.sin(week * 0.5) * 2).toFloat(),
                    timestamp = now - ((15 - week) * 7 * 24 * 3600)
                )
            },
            keyFactors = listOf(
                YieldFactor(
                    name = "Soil Moisture",
                    impact = "positive",
                    value = "52% avg",
                    description = "Maintained in optimal range during grand growth phase"
                ),
                YieldFactor(
                    name = "Growing Degree Days",
                    impact = "positive",
                    value = "2,450 GDD",
                    description = "Above average accumulated heat units supporting growth"
                ),
                YieldFactor(
                    name = "Precipitation",
                    impact = "neutral",
                    value = "185mm",
                    description = "Adequate rainfall supplemented by irrigation"
                ),
                YieldFactor(
                    name = "Nutrient Status",
                    impact = "positive",
                    value = "Good",
                    description = "Timely fertilizer applications as per AI recommendations"
                )
            ),
            aiInsight = "Yield projection increased 2.1% this week to 68.5 tonnes/hectare. Optimal soil moisture management during the grand growth phase is the primary driver. Continue current irrigation schedule. Expected harvest window: Late March to Mid April for maximum sugar recovery.",
            lastUpdated = now,
            isLoading = false
        )
    }

    fun refresh() {
        loadYieldPrediction()
    }
}
