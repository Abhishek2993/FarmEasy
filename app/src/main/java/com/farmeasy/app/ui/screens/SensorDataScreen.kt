package com.farmeasy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.ui.components.SensorCard
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.formatDecimal
import com.farmeasy.app.utils.getMoistureColor
import com.farmeasy.app.utils.getMoistureLabel
import com.farmeasy.app.utils.getRainfallLabel
import com.farmeasy.app.utils.getTemperatureColor
import com.farmeasy.app.utils.getTemperatureLabel
import com.farmeasy.app.utils.toRelativeTime
import com.farmeasy.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDataScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sensor = uiState.sensorData

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Readings", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.lastUpdated?.let { ts ->
                Text(
                    text = "Last updated: ${ts.toRelativeTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (sensor != null) {
                SensorCard(
                    title = "Soil Moisture",
                    value = sensor.soilMoisture.formatDecimal(),
                    unit = "%",
                    icon = Icons.Default.WaterDrop,
                    statusColor = getMoistureColor(sensor.soilMoisture),
                    statusLabel = getMoistureLabel(sensor.soilMoisture)
                )

                SensorCard(
                    title = "Soil Temperature",
                    value = sensor.soilTemperature.formatDecimal(),
                    unit = "°C",
                    icon = Icons.Default.DeviceThermostat,
                    statusColor = getTemperatureColor(sensor.soilTemperature),
                    statusLabel = getTemperatureLabel(sensor.soilTemperature)
                )

                SensorCard(
                    title = "Ambient Temperature",
                    value = sensor.ambientTemp.formatDecimal(),
                    unit = "°C",
                    icon = Icons.Default.Thermostat,
                    statusColor = getTemperatureColor(sensor.ambientTemp),
                    statusLabel = getTemperatureLabel(sensor.ambientTemp)
                )

                SensorCard(
                    title = "Humidity",
                    value = sensor.humidity.formatDecimal(),
                    unit = "%",
                    icon = Icons.Default.Opacity,
                    statusColor = MossGreen,
                    statusLabel = "${sensor.humidity.formatDecimal()}%"
                )

                SensorCard(
                    title = "Rainfall",
                    value = sensor.rainfallMm.formatDecimal(),
                    unit = "mm",
                    icon = Icons.Default.Cloud,
                    statusColor = if (sensor.rainfallMm > 0) MoistureWaterlogged else MossGreen,
                    statusLabel = getRainfallLabel(sensor.rainfallMm)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SensorCard(
                        title = "Valve",
                        value = if (sensor.valveStatus) "OPEN" else "CLOSED",
                        unit = "",
                        icon = if (sensor.valveStatus) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                        statusColor = if (sensor.valveStatus) MoistureWaterlogged else ForestGreen,
                        statusLabel = if (sensor.valveStatus) "Active" else "Inactive",
                        modifier = Modifier.weight(1f)
                    )

                    SensorCard(
                        title = "Battery",
                        value = "${sensor.batteryPct}",
                        unit = "%",
                        icon = Icons.Default.BatteryFull,
                        statusColor = if (sensor.batteryPct > 20) SuccessGreen else ErrorRed,
                        statusLabel = if (sensor.batteryPct > 20) "Good" else "Low",
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = "No sensor data available. Connect to your ESP32 device via Bluetooth.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
