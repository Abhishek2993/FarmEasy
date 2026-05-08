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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.farmeasy.app.ui.components.ChartDataPoint
import com.farmeasy.app.ui.components.SensorLineChart
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.formatDecimal
import com.farmeasy.app.viewmodel.HistoryViewModel
import com.farmeasy.app.viewmodel.TimeRange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History & Analytics", style = MaterialTheme.typography.headlineSmall) }
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
            // Time Range Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    FilterChip(
                        selected = uiState.selectedTimeRange == range,
                        onClick = { viewModel.selectTimeRange(range) },
                        label = {
                            Text(
                                text = range.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            // Soil Moisture Chart
            Text(
                text = "Soil Moisture",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val moistureData = uiState.readings.map {
                        ChartDataPoint(x = it.timestamp.toFloat(), y = it.soilMoisture)
                    }
                    if (moistureData.isNotEmpty()) {
                        SensorLineChart(
                            data = moistureData,
                            lineColor = ForestGreen,
                            fillColor = MossGreen,
                            label = "Moisture %",
                            chartHeight = 220.dp
                        )
                    } else {
                        Text(
                            text = "No data available for this time range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }

            // Soil Temperature Chart
            Text(
                text = "Soil Temperature",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val tempData = uiState.readings.map {
                        ChartDataPoint(x = it.timestamp.toFloat(), y = it.soilTemperature)
                    }
                    if (tempData.isNotEmpty()) {
                        SensorLineChart(
                            data = tempData,
                            lineColor = WarmAmber,
                            fillColor = WarningYellowLight,
                            label = "Temperature °C",
                            chartHeight = 220.dp
                        )
                    } else {
                        Text(
                            text = "No data available for this time range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }

            // Statistics
            val stats = uiState.statistics
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Moisture",
                    min = stats.minMoisture?.formatDecimal() ?: "—",
                    max = stats.maxMoisture?.formatDecimal() ?: "—",
                    avg = stats.avgMoisture?.formatDecimal() ?: "—",
                    unit = "%",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Temperature",
                    min = stats.minTemperature?.formatDecimal() ?: "—",
                    max = stats.maxTemperature?.formatDecimal() ?: "—",
                    avg = stats.avgTemperature?.formatDecimal() ?: "—",
                    unit = "°C",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    min: String,
    max: String,
    avg: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatRow("Min", "$min $unit")
            StatRow("Max", "$max $unit")
            StatRow("Avg", "$avg $unit")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
