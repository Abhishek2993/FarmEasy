package com.farmeasy.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.farmeasy.app.utils.Constants
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BleManager"

        // BLE Service and Characteristic UUIDs for FarmEasy ESP32
        val SERVICE_UUID: UUID = UUID.fromString(Constants.BLE_SERVICE_UUID)
        val SENSOR_DATA_CHAR_UUID: UUID = UUID.fromString(Constants.BLE_SENSOR_DATA_CHAR_UUID)
        val VALVE_COMMAND_CHAR_UUID: UUID = UUID.fromString(Constants.BLE_VALVE_COMMAND_CHAR_UUID)
        val CLIENT_CONFIG_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var bleScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val gson = Gson()

    // State flows
    private val _connectionState = MutableStateFlow(BleConnectionState.IDLE)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDeviceModel>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDeviceModel>> = _discoveredDevices.asStateFlow()

    private val _connectedDevice = MutableStateFlow<BleDeviceModel?>(null)
    val connectedDevice: StateFlow<BleDeviceModel?> = _connectedDevice.asStateFlow()

    private val _sensorData = MutableSharedFlow<SensorPacket>(replay = 1)
    val sensorData: SharedFlow<SensorPacket> = _sensorData.asSharedFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    // Scan callback
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val bleDevice = BleDeviceModel(
                name = device.name,
                address = device.address,
                rssi = result.rssi
            )

            val currentList = _discoveredDevices.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == bleDevice.address }
            if (existingIndex != -1) {
                currentList[existingIndex] = bleDevice
            } else {
                currentList.add(bleDevice)
            }
            _discoveredDevices.value = currentList
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            _connectionState.value = BleConnectionState.ERROR
        }
    }

    // GATT callback
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server")
                    _connectionState.value = BleConnectionState.CONNECTED
                    _connectedDevice.value = _connectedDevice.value?.copy(
                        isConnected = true,
                        isConnecting = false
                    )
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server")
                    _connectionState.value = BleConnectionState.DISCONNECTED
                    _connectedDevice.value = _connectedDevice.value?.copy(
                        isConnected = false,
                        isConnecting = false
                    )
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered")
                // Enable notifications on sensor data characteristic
                val service = gatt.getService(SERVICE_UUID)
                val sensorCharacteristic = service?.getCharacteristic(SENSOR_DATA_CHAR_UUID)
                if (sensorCharacteristic != null) {
                    gatt.setCharacteristicNotification(sensorCharacteristic, true)
                    val descriptor = sensorCharacteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR)
                    descriptor?.let {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(it)
                    }
                } else {
                    Log.e(TAG, "Sensor data characteristic not found")
                }
            }
        }

        @Deprecated("Deprecated in API 33")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == SENSOR_DATA_CHAR_UUID) {
                val data = characteristic.getStringValue(0)
                parseSensorData(data)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == SENSOR_DATA_CHAR_UUID) {
                val data = String(value, Charsets.UTF_8)
                parseSensorData(data)
            }
        }
    }

    private fun parseSensorData(data: String) {
        try {
            val packet = gson.fromJson(data, SensorPacket::class.java)
            _sensorData.tryEmit(packet)
            Log.d(TAG, "Parsed sensor data: moisture=${packet.soilMoisture}, temp=${packet.soilTemperature}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse sensor data: $data", e)
            // Try CSV fallback parsing
            tryParseCsv(data)
        }
    }

    private fun tryParseCsv(data: String) {
        try {
            val parts = data.split(",")
            if (parts.size >= 8) {
                val packet = SensorPacket(
                    nodeId = parts[0].trim().toInt(),
                    soilMoisture = parts[1].trim().toFloat(),
                    soilTemperature = parts[2].trim().toFloat(),
                    ambientTemp = parts[3].trim().toFloat(),
                    humidity = parts[4].trim().toFloat(),
                    rainfallMm = parts[5].trim().toFloat(),
                    valveStatus = parts[6].trim().toBoolean(),
                    batteryPct = parts[7].trim().toInt(),
                    timestamp = if (parts.size > 8) parts[8].trim().toLong() else System.currentTimeMillis() / 1000
                )
                _sensorData.tryEmit(packet)
            }
        } catch (e: Exception) {
            Log.e(TAG, "CSV parsing also failed: $data", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!isBluetoothEnabled) {
            _error.tryEmit("Bluetooth is not enabled")
            return
        }

        _discoveredDevices.value = emptyList()
        _connectionState.value = BleConnectionState.SCANNING

        bleScanner = bluetoothAdapter?.bluetoothLeScanner
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner?.startScan(null, settings, scanCallback)
        Log.i(TAG, "BLE scan started")
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bleScanner?.stopScan(scanCallback)
        if (_connectionState.value == BleConnectionState.SCANNING) {
            _connectionState.value = BleConnectionState.IDLE
        }
        Log.i(TAG, "BLE scan stopped")
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BleDeviceModel) {
        stopScan()
        _connectionState.value = BleConnectionState.CONNECTING
        _connectedDevice.value = device.copy(isConnecting = true)

        val bluetoothDevice: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(device.address)

        if (bluetoothDevice == null) {
            _connectionState.value = BleConnectionState.ERROR
            _error.tryEmit("Device not found: ${device.address}")
            return
        }

        bluetoothGatt = bluetoothDevice.connectGatt(context, false, gattCallback)
        Log.i(TAG, "Connecting to ${device.displayName} (${device.address})")
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleConnectionState.DISCONNECTED
        _connectedDevice.value = null
        Log.i(TAG, "Disconnected")
    }

    @SuppressLint("MissingPermission")
    fun sendValveCommand(zone: Int, open: Boolean) {
        val gatt = bluetoothGatt ?: run {
            _error.tryEmit("Not connected to device")
            return
        }

        val service = gatt.getService(SERVICE_UUID)
        val valveChar = service?.getCharacteristic(VALVE_COMMAND_CHAR_UUID)

        if (valveChar != null) {
            val command = """{"zone":$zone,"action":"${if (open) "open" else "close"}"}"""
            valveChar.setValue(command.toByteArray(Charsets.UTF_8))
            gatt.writeCharacteristic(valveChar)
            Log.i(TAG, "Sent valve command: $command")
        } else {
            _error.tryEmit("Valve control not available")
        }
    }

    @SuppressLint("MissingPermission")
    fun requestManualRead() {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(SERVICE_UUID)
        val sensorChar = service?.getCharacteristic(SENSOR_DATA_CHAR_UUID)
        if (sensorChar != null) {
            gatt.readCharacteristic(sensorChar)
        }
    }

    fun cleanup() {
        stopScan()
        disconnect()
    }
}
