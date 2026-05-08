package com.farmeasy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.data.remote.ForecastItem
import com.farmeasy.app.data.remote.WeatherAlert
import com.farmeasy.app.data.remote.WeatherCurrentResponse
import com.farmeasy.app.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val currentWeather: WeatherCurrentResponse? = null,
    val forecast: List<ForecastItem> = emptyList(),
    val alerts: List<WeatherAlert> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadWeatherData()
    }

    private fun loadWeatherData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val currentResult = weatherRepository.getCurrentWeather()
            val forecastResult = weatherRepository.getWeatherForecast()

            currentResult.onSuccess { weather ->
                _uiState.value = _uiState.value.copy(currentWeather = weather)
            }

            forecastResult.onSuccess { forecast ->
                _uiState.value = _uiState.value.copy(
                    forecast = forecast.forecasts,
                    alerts = forecast.alerts
                )
            }

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun refresh() {
        loadWeatherData()
    }
}
