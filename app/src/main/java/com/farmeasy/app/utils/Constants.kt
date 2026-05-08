package com.farmeasy.app.utils

object Constants {
    // Cloud API
    const val BASE_URL = "https://api.farmeasy.in/v1/"
    const val API_TIMEOUT_SECONDS = 30L

    // BLE UUIDs (FarmEasy ESP32 custom service)
    const val BLE_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    const val BLE_SENSOR_DATA_CHAR_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    const val BLE_VALVE_COMMAND_CHAR_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a9"

    // BLE Scan
    const val BLE_SCAN_TIMEOUT_MS = 15000L
    const val BLE_AUTO_RECONNECT_DELAY_MS = 5000L

    // Default Sensor Thresholds
    const val MOISTURE_CRITICAL_LOW = 20f
    const val MOISTURE_LOW = 40f
    const val MOISTURE_OPTIMAL_HIGH = 70f
    const val MOISTURE_HIGH = 75f

    const val TEMP_COLD_STRESS = 15f
    const val TEMP_OPTIMAL_HIGH = 35f
    const val TEMP_HEAT_STRESS = 40f

    const val RAINFALL_HEAVY_THRESHOLD = 30f // mm

    // Crop Stages (days from planting)
    const val FORMATIVE_PHASE_END = 120
    const val GRAND_GROWTH_PHASE_END = 270

    // Sync
    const val SYNC_INTERVAL_HOURS = 1L
    const val DATA_RETENTION_DAYS = 90L

    // DataStore Keys
    const val PREF_LANGUAGE = "language"
    const val PREF_TEMPERATURE_UNIT = "temperature_unit"
    const val PREF_AREA_UNIT = "area_unit"
    const val PREF_LAST_DEVICE_ADDRESS = "last_device_address"
    const val PREF_LAST_DEVICE_NAME = "last_device_name"
    const val PREF_AUTO_RECONNECT = "auto_reconnect"
    const val PREF_FARM_ID = "farm_id"
    const val PREF_PLANTING_DATE = "planting_date"
    const val PREF_FARM_LAT = "farm_latitude"
    const val PREF_FARM_LON = "farm_longitude"
    const val PREF_LAST_SYNC = "last_sync_timestamp"
    const val PREF_NODE_ID = "node_id"

    // Notification Channels
    const val CHANNEL_ALERTS = "farm_alerts"
    const val CHANNEL_IRRIGATION = "irrigation_updates"
    const val CHANNEL_SYSTEM = "system_health"

    // Notification Preference Keys
    const val PREF_NOTIFY_WEATHER = "notify_weather"
    const val PREF_NOTIFY_IRRIGATION = "notify_irrigation"
    const val PREF_NOTIFY_FERTILIZER = "notify_fertilizer"
    const val PREF_NOTIFY_YIELD = "notify_yield"
    const val PREF_NOTIFY_SYSTEM = "notify_system"
    const val PREF_NOTIFY_MARKET = "notify_market"

    // Alert Threshold Keys
    const val PREF_THRESHOLD_MOISTURE_LOW = "threshold_moisture_low"
    const val PREF_THRESHOLD_MOISTURE_HIGH = "threshold_moisture_high"
    const val PREF_THRESHOLD_TEMP_LOW = "threshold_temp_low"
    const val PREF_THRESHOLD_TEMP_HIGH = "threshold_temp_high"
    const val PREF_THRESHOLD_RAIN = "threshold_rain"

    // Default Farm Values
    const val DEFAULT_NODE_ID = 1
    const val DEFAULT_FARM_ID = 1
    const val DEFAULT_LATITUDE = 18.5204
    const val DEFAULT_LONGITUDE = 73.8567 // Pune, Maharashtra
    const val DEFAULT_REGION = "pune"
}
