package com.farmeasy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.bluetooth.BleConnectionState
import com.farmeasy.app.ui.components.AlertBanner
import com.farmeasy.app.ui.components.ConnectionStatusIndicator
import com.farmeasy.app.ui.components.GaugeWidget
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
fun DashboardScreen(
    onNavigateToBluetooth: () -> Unit = {},
    onNavigateToYield: () -> Unit = {},
    onNavigateToIrrigation: () -> Unit = {},
    onNavigateToWeather: () -> Unit = {},
    onNavigateToSensorData: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val unreadCount by viewModel.unreadAlertCount.collectAsState()
    val sensor = uiState.sensorData

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "FarmEasy",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Farm Dashboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigateToBluetooth) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth",
                        tint = if (uiState.connectionState == BleConnectionState.CONNECTED)
                            ConnectionOnline else ConnectionOffline
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Offline Banner
        AlertBanner(
            message = "Working offline — data will sync when connected",
            isVisible = uiState.isOffline,
            backgroundColor = CardAmberTint,
            contentColor = WarmAmberDark
        )

        if (uiState.isLoading && sensor == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = { viewModel.refreshData() },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Connection Status
                    ConnectionStatusIndicator(
                        isConnected = uiState.connectionState == BleConnectionState.CONNECTED,
                        deviceName = uiState.connectedDeviceName
                    )

                    // Last Updated
                    uiState.lastUpdated?.let { ts ->
                        Text(
                            text = "Last updated: ${ts.toRelativeTime()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Soil Moisture Gauge
                    if (sensor != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Soil Moisture",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                GaugeWidget(
                                    value = sensor.soilMoisture,
                                    label = getMoistureLabel(sensor.soilMoisture),
                                    unit = "%",
                                    gaugeColor = getMoistureColor(sensor.soilMoisture),
                                    size = 180.dp
                                )
                            }
                        }

                        // Sensor Data Cards — 2 column grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SensorCard(
                                title = "Soil Temp",
                                value = sensor.soilTemperature.formatDecimal(),
                                unit = "°C",
                                icon = Icons.Default.DeviceThermostat,
                                statusColor = getTemperatureColor(sensor.soilTemperature),
                                statusLabel = getTemperatureLabel(sensor.soilTemperature),
                                modifier = Modifier.weight(1f)
                            )
                            SensorCard(
                                title = "Ambient",
                                value = sensor.ambientTemp.formatDecimal(),
                                unit = "°C",
                                icon = Icons.Default.Thermostat,
                                statusColor = getTemperatureColor(sensor.ambientTemp),
                                statusLabel = "${sensor.ambientTemp.formatDecimal()}°C",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SensorCard(
                                title = "Humidity",
                                value = sensor.humidity.formatDecimal(),
                                unit = "%",
                                icon = Icons.Default.Opacity,
                                statusColor = MossGreen,
                                statusLabel = "${sensor.humidity.formatDecimal()}%",
                                modifier = Modifier.weight(1f)
                            )
                            SensorCard(
                                title = "Rainfall",
                                value = if (sensor.rainfallMm > 0) sensor.rainfallMm.formatDecimal() else "0.0",
                                unit = "mm",
                                icon = Icons.Default.Cloud,
                                statusColor = if (sensor.rainfallMm > 0) MoistureWaterlogged else MossGreen,
                                statusLabel = getRainfallLabel(sensor.rainfallMm),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Quick Info Cards
                    Text(
                        text = "Quick Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Crop Stage Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardGreenTint)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Current Crop Stage",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ForestGreenDark
                                )
                                Text(
                                    text = uiState.cropStage,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                                Text(
                                    text = "Day ${uiState.daysSincePlanting} since planting",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ForestGreenDark.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Yield Projection Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardAmberTint),
                        onClick = onNavigateToYield
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = WarmAmber,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Yield Projection",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WarmAmberDark
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = uiState.yieldProjection.formatDecimal(),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmAmberDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "t/ha",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = WarmAmberDark.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (uiState.yieldTrend == "up") {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Trending up",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Next Irrigation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBlueTint),
                        onClick = onNavigateToIrrigation
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = MoistureWaterlogged,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Next Irrigation",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoistureWaterlogged
                                )
                                Text(
                                    text = uiState.nextIrrigation,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MoistureWaterlogged
                                )
                            }
                        }
                    }

                    // Battery Status (if sensor data available)
                    sensor?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (it.batteryPct <= 20) CardRedTint else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ESP32 Battery",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${it.batteryPct}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (it.batteryPct <= 20) ErrorRed else SuccessGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
                }
            }
        }
    }
}
