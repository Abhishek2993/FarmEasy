package com.farmeasy.app.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmeasy.app.bluetooth.BleConnectionState
import com.farmeasy.app.bluetooth.BleDeviceModel
import com.farmeasy.app.bluetooth.BleManager
import com.farmeasy.app.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BluetoothUiState(
    val connectionState: BleConnectionState = BleConnectionState.IDLE,
    val discoveredDevices: List<BleDeviceModel> = emptyList(),
    val connectedDevice: BleDeviceModel? = null,
    val lastDeviceAddress: String? = null,
    val lastDeviceName: String? = null,
    val errorMessage: String? = null,
    val showHelp: Boolean = false,
    val permissionsGranted: Boolean = false,
    val scanTimeoutSeconds: Int = 15
)

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bleManager: BleManager,
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState: StateFlow<BluetoothUiState> = _uiState.asStateFlow()

    private var scanTimeoutJob: Job? = null

    private val lastDeviceAddressKey = stringPreferencesKey(Constants.PREF_LAST_DEVICE_ADDRESS)
    private val lastDeviceNameKey = stringPreferencesKey(Constants.PREF_LAST_DEVICE_NAME)

    init {
        observeBleState()
        loadLastDevice()
        checkPermissions()
    }

    private fun observeBleState() {
        viewModelScope.launch {
            bleManager.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }
        viewModelScope.launch {
            bleManager.discoveredDevices.collect { devices ->
                _uiState.value = _uiState.value.copy(discoveredDevices = devices)
            }
        }
        viewModelScope.launch {
            bleManager.connectedDevice.collect { device ->
                _uiState.value = _uiState.value.copy(connectedDevice = device)
                if (device?.isConnected == true) {
                    saveLastDevice(device)
                }
            }
        }
        viewModelScope.launch {
            bleManager.error.collect { error ->
                _uiState.value = _uiState.value.copy(errorMessage = error)
            }
        }
    }

    private fun loadLastDevice() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            _uiState.value = _uiState.value.copy(
                lastDeviceAddress = prefs[lastDeviceAddressKey],
                lastDeviceName = prefs[lastDeviceNameKey]
            )
        }
    }

    private suspend fun saveLastDevice(device: BleDeviceModel) {
        dataStore.edit { prefs ->
            prefs[lastDeviceAddressKey] = device.address
            prefs[lastDeviceNameKey] = device.displayName
        }
        _uiState.value = _uiState.value.copy(
            lastDeviceAddress = device.address,
            lastDeviceName = device.displayName
        )
    }

    fun checkPermissions() {
        val permissions = getRequiredPermissions()
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        _uiState.value = _uiState.value.copy(permissionsGranted = allGranted)
    }

    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        return permissions
    }

    fun onPermissionsResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(permissionsGranted = granted)
    }

    fun startScan() {
        if (!bleManager.isBluetoothSupported) {
            _uiState.value = _uiState.value.copy(errorMessage = "Bluetooth not supported on this device")
            return
        }
        if (!bleManager.isBluetoothEnabled) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enable Bluetooth")
            return
        }

        bleManager.startScan()

        // Auto-stop scan after timeout
        scanTimeoutJob?.cancel()
        scanTimeoutJob = viewModelScope.launch {
            delay(Constants.BLE_SCAN_TIMEOUT_MS)
            stopScan()
        }
    }

    fun stopScan() {
        scanTimeoutJob?.cancel()
        bleManager.stopScan()
    }

    fun connectToDevice(device: BleDeviceModel) {
        bleManager.connect(device)
    }

    fun reconnectLastDevice() {
        val address = _uiState.value.lastDeviceAddress ?: return
        val name = _uiState.value.lastDeviceName
        val device = BleDeviceModel(name = name, address = address)
        bleManager.connect(device)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun toggleHelp() {
        _uiState.value = _uiState.value.copy(showHelp = !_uiState.value.showHelp)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        scanTimeoutJob?.cancel()
    }
}
