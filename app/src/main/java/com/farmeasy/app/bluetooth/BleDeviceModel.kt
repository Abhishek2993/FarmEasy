package com.farmeasy.app.bluetooth

data class BleDeviceModel(
    val name: String?,
    val address: String,
    val rssi: Int = 0,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false
) {
    val displayName: String
        get() = name ?: "Unknown Device"

    val signalStrength: SignalStrength
        get() = when {
            rssi >= -50 -> SignalStrength.STRONG
            rssi >= -70 -> SignalStrength.MEDIUM
            rssi >= -85 -> SignalStrength.WEAK
            else -> SignalStrength.VERY_WEAK
        }
}

enum class SignalStrength {
    STRONG, MEDIUM, WEAK, VERY_WEAK
}

enum class BleConnectionState {
    IDLE,
    SCANNING,
    FOUND,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR
}

data class SensorPacket(
    val nodeId: Int,
    val soilMoisture: Float,
    val soilTemperature: Float,
    val ambientTemp: Float,
    val humidity: Float,
    val rainfallMm: Float,
    val valveStatus: Boolean,
    val batteryPct: Int,
    val timestamp: Long
)
