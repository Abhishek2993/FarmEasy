package com.farmeasy.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.formatDecimal
import com.farmeasy.app.utils.toFormattedTime
import com.farmeasy.app.viewmodel.IrrigationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IrrigationScreen(
    viewModel: IrrigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showOverrideDialog by remember { mutableStateOf(false) }
    var selectedZone by remember { mutableStateOf(1) }

    LaunchedEffect(uiState.overrideResult) {
        uiState.overrideResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOverrideResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Irrigation Management", style = MaterialTheme.typography.headlineSmall)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Smart Message
            item {
                uiState.smartMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBlueTint)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "💡", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MoistureWaterlogged
                            )
                        }
                    }
                }
            }

            // Zone Status
            item {
                Text(
                    text = "Zone Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(uiState.zoneStatuses) { zone ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (zone.valveOpen) CardBlueTint else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (zone.valveOpen) MoistureWaterlogged else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Zone ${zone.zone}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (zone.valveOpen) "Valve OPEN${zone.durationMinutes?.let { " • ${it}min" } ?: ""}" else "Valve CLOSED",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (zone.valveOpen) MoistureWaterlogged else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                selectedZone = zone.zone
                                showOverrideDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (zone.valveOpen) ErrorRed else ForestGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                if (zone.valveOpen) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (zone.valveOpen) "Stop" else "Start")
                        }
                    }
                }
            }

            // Water Usage
            item {
                Text(
                    text = "Water Usage This Week",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${uiState.weeklyWaterUsage.formatDecimal(0)} L",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MoistureWaterlogged
                            )
                            Text(
                                text = "/ ${uiState.weeklyWaterTarget.formatDecimal(0)} L target",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (uiState.weeklyWaterUsage / uiState.weeklyWaterTarget).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MoistureWaterlogged,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Today's Schedule
            item {
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.todaySchedule) { slot ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (slot.status) {
                                "completed" -> Icons.Default.Check
                                "in_progress" -> Icons.Default.WaterDrop
                                "skipped" -> Icons.Default.SkipNext
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            tint = when (slot.status) {
                                "completed" -> SuccessGreen
                                "in_progress" -> MoistureWaterlogged
                                "skipped" -> Color.Gray
                                else -> WarmAmber
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zone ${slot.zone}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${slot.startTime.toFormattedTime()} — ${slot.endTime.toFormattedTime()} (${slot.durationMinutes}min)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = slot.status.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = when (slot.status) {
                                "completed" -> SuccessGreen
                                "in_progress" -> MoistureWaterlogged
                                "skipped" -> Color.Gray
                                else -> WarmAmber
                            }
                        )
                    }
                }
            }

            // Irrigation History
            if (uiState.irrigationHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(uiState.irrigationHistory.take(5)) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Zone ${event.zone} • ${event.durationMinutes}min",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (event.isAiAutomated) "🤖 AI Scheduled" else "👨‍🌾 Manual Override",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            event.waterVolumeLiters?.let {
                                Text(
                                    text = "${it.formatDecimal()} L",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MoistureWaterlogged
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Override Confirmation Dialog
    if (showOverrideDialog) {
        val zone = uiState.zoneStatuses.find { it.zone == selectedZone }
        val isOpen = zone?.valveOpen == true
        AlertDialog(
            onDismissRequest = { showOverrideDialog = false },
            title = { Text("Manual Override") },
            text = {
                Text(
                    if (isOpen) "Stop irrigation on Zone $selectedZone?"
                    else "Start 30-minute irrigation on Zone $selectedZone?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.overrideIrrigation(
                            zone = selectedZone,
                            action = if (isOpen) "stop" else "start"
                        )
                        showOverrideDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverrideDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
