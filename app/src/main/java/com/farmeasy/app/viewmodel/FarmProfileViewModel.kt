package com.farmeasy.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.data.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FarmProfileUiState(
    val farmName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val areaAcres: String = "",
    val sugarcaneVariety: String = "Co-86032",
    val plantingSeason: String = "Suru (Jan-Feb)",
    val plantingDate: Long = System.currentTimeMillis(),
    val ratoonCycle: Int = 0,
    val irrigationZones: String = "2",
    val waterSource: String = "Borewell",
    val electricityWindow: String = "10 PM - 6 AM",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        val VARIETIES = listOf(
            "Co-86032", "CoM-0265", "Co-92005", "CoC-671",
            "Co-0238", "Co-0118", "CoVSI-9805", "Other"
        )
        val SEASONS = listOf(
            "Suru (Jan-Feb)", "Adsali (Jul-Aug)", "Pre-seasonal (Oct-Nov)"
        )
        val WATER_SOURCES = listOf(
            "Borewell", "Canal", "River", "Well", "Drip Irrigation", "Other"
        )
        val RATOON_OPTIONS = listOf(
            0 to "Plant Crop (Fresh)",
            1 to "1st Ratoon",
            2 to "2nd Ratoon",
            3 to "3rd Ratoon",
            4 to "4th+ Ratoon"
        )
    }
}

@HiltViewModel
class FarmProfileViewModel @Inject constructor(
    private val farmRepository: FarmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmProfileUiState())
    val uiState: StateFlow<FarmProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            farmRepository.farmProfile.collect { profile ->
                _uiState.value = _uiState.value.copy(
                    farmName = profile.farmName,
                    latitude = profile.latitude,
                    longitude = profile.longitude,
                    areaAcres = profile.areaAcres.toString(),
                    sugarcaneVariety = profile.sugarcaneVariety,
                    plantingSeason = profile.plantingSeason,
                    plantingDate = profile.plantingDate,
                    ratoonCycle = profile.ratoonCycle,
                    irrigationZones = profile.irrigationZones.toString(),
                    waterSource = profile.waterSource,
                    electricityWindow = profile.electricityWindow
                )
            }
        }
    }

    fun updateFarmName(value: String) {
        _uiState.value = _uiState.value.copy(farmName = value)
    }

    fun updateArea(value: String) {
        _uiState.value = _uiState.value.copy(areaAcres = value)
    }

    fun updateVariety(value: String) {
        _uiState.value = _uiState.value.copy(sugarcaneVariety = value)
    }

    fun updateSeason(value: String) {
        _uiState.value = _uiState.value.copy(plantingSeason = value)
    }

    fun updatePlantingDate(value: Long) {
        _uiState.value = _uiState.value.copy(plantingDate = value)
    }

    fun updateRatoonCycle(value: Int) {
        _uiState.value = _uiState.value.copy(ratoonCycle = value)
    }

    fun updateIrrigationZones(value: String) {
        _uiState.value = _uiState.value.copy(irrigationZones = value)
    }

    fun updateWaterSource(value: String) {
        _uiState.value = _uiState.value.copy(waterSource = value)
    }

    fun updateElectricityWindow(value: String) {
        _uiState.value = _uiState.value.copy(electricityWindow = value)
    }

    fun updateLocation(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(latitude = lat, longitude = lon)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            try {
                val state = _uiState.value
                val profile = FarmRepository.FarmProfile(
                    farmName = state.farmName,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    areaAcres = state.areaAcres.toFloatOrNull() ?: 3f,
                    sugarcaneVariety = state.sugarcaneVariety,
                    plantingSeason = state.plantingSeason,
                    plantingDate = state.plantingDate,
                    ratoonCycle = state.ratoonCycle,
                    irrigationZones = state.irrigationZones.toIntOrNull() ?: 2,
                    waterSource = state.waterSource,
                    electricityWindow = state.electricityWindow
                )
                farmRepository.saveFarmProfile(profile)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save profile"
                )
            }
        }
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
