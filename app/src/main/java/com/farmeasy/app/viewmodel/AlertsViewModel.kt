package com.farmeasy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.data.local.AlertEntity
import com.farmeasy.app.data.repository.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertsUiState(
    val selectedCategory: String? = null, // null = all
    val isLoading: Boolean = true
)

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    val allAlerts = alertRepository.getAllAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount = alertRepository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            alertRepository.fetchFromCloud()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun markAsRead(alertId: Long) {
        viewModelScope.launch {
            alertRepository.markAsRead(alertId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            alertRepository.markAllAsRead()
        }
    }

    fun refresh() {
        loadAlerts()
    }

    companion object {
        val CATEGORIES = listOf(
            "weather" to "Weather",
            "irrigation" to "Irrigation",
            "fertilizer" to "Fertilizer",
            "yield" to "Yield",
            "system" to "System",
            "market" to "Market"
        )
    }
}
