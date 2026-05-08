package com.farmeasy.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.formatDecimal
import com.farmeasy.app.utils.toFormattedTime
import com.farmeasy.app.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather", style = MaterialTheme.typography.headlineSmall) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Weather
            uiState.currentWeather?.let { weather ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBlueTint)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = WarmAmber,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${weather.temperature.formatDecimal(0)}°C",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = weather.description,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherStat(icon = Icons.Default.Opacity, value = "${weather.humidity.formatDecimal(0)}%", label = "Humidity")
                            WeatherStat(icon = Icons.Default.Air, value = "${weather.windSpeed.formatDecimal(1)} km/h", label = "Wind")
                            WeatherStat(icon = Icons.Default.Cloud, value = "${weather.cloudCover}%", label = "Clouds")
                            WeatherStat(icon = Icons.Default.WaterDrop, value = "${weather.rainProbability}%", label = "Rain")
                        }
                    }
                }

                // AI Recommendation
                weather.recommendation?.let { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardGreenTint)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text(text = "🌱", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = rec,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ForestGreenDark
                            )
                        }
                    }
                }
            }

            // 48-Hour Forecast
            if (uiState.forecast.isNotEmpty()) {
                Text(
                    text = "48-Hour Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Show every 3 hours
                    uiState.forecast.filterIndexed { index, _ -> index % 3 == 0 }.forEach { item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.rainProbability > 50)
                                    CardBlueTint else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .width(80.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = item.timestamp.toFormattedTime(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Icon(
                                    imageVector = if (item.rainProbability > 50) Icons.Default.Cloud
                                    else Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (item.rainProbability > 50) MoistureWaterlogged else WarmAmber,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${item.temperature.formatDecimal(0)}°",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${item.rainProbability}% 🌧",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MoistureWaterlogged
                                )
                            }
                        }
                    }
                }
            }

            // Weather Alerts
            if (uiState.alerts.isNotEmpty()) {
                Text(
                    text = "Weather Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                uiState.alerts.forEach { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (alert.severity) {
                                "critical" -> CardRedTint
                                "warning" -> CardAmberTint
                                else -> CardBlueTint
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (alert.severity) {
                                    "critical" -> ErrorRed
                                    "warning" -> WarmAmberDark
                                    else -> MoistureWaterlogged
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = alert.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WeatherStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
