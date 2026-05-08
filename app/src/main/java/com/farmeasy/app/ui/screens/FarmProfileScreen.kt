package com.farmeasy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.toFormattedDate
import com.farmeasy.app.viewmodel.FarmProfileUiState
import com.farmeasy.app.viewmodel.FarmProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmProfileScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: FarmProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Farm profile saved successfully!")
            viewModel.clearSaveSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farm Profile", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Farm Name
            OutlinedTextField(
                value = uiState.farmName,
                onValueChange = { viewModel.updateFarmName(it) },
                label = { Text("Farm Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Location
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Lat: ${String.format("%.4f", uiState.latitude)}, Lon: ${String.format("%.4f", uiState.longitude)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    OutlinedButton(
                        onClick = { /* GPS auto-detect - requires location permission */ },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Detect")
                    }
                }
            }

            // Area
            OutlinedTextField(
                value = uiState.areaAcres,
                onValueChange = { viewModel.updateArea(it) },
                label = { Text("Farm Area (Acres)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Sugarcane Variety Dropdown
            DropdownField(
                label = "Sugarcane Variety",
                selected = uiState.sugarcaneVariety,
                options = FarmProfileUiState.VARIETIES,
                onSelect = { viewModel.updateVariety(it) }
            )

            // Planting Season Dropdown
            DropdownField(
                label = "Planting Season",
                selected = uiState.plantingSeason,
                options = FarmProfileUiState.SEASONS,
                onSelect = { viewModel.updateSeason(it) }
            )

            // Planting Date
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Planting Date",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = (uiState.plantingDate / 1000).toFormattedDate(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(text = "Change", color = MaterialTheme.colorScheme.primary)
                }
            }

            // Ratoon Cycle Dropdown
            DropdownField(
                label = "Ratoon Cycle",
                selected = FarmProfileUiState.RATOON_OPTIONS.find { it.first == uiState.ratoonCycle }?.second ?: "Plant Crop",
                options = FarmProfileUiState.RATOON_OPTIONS.map { it.second },
                onSelect = { selected ->
                    val cycle = FarmProfileUiState.RATOON_OPTIONS.find { it.second == selected }?.first ?: 0
                    viewModel.updateRatoonCycle(cycle)
                }
            )

            // Irrigation Zones
            OutlinedTextField(
                value = uiState.irrigationZones,
                onValueChange = { viewModel.updateIrrigationZones(it) },
                label = { Text("Number of Irrigation Zones") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Water Source
            DropdownField(
                label = "Water Source",
                selected = uiState.waterSource,
                options = FarmProfileUiState.WATER_SOURCES,
                onSelect = { viewModel.updateWaterSource(it) }
            )

            // Electricity Window
            OutlinedTextField(
                value = uiState.electricityWindow,
                onValueChange = { viewModel.updateElectricityWindow(it) },
                label = { Text("Electricity Availability Window") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("e.g., 10 PM - 6 AM") }
            )

            // Save Button
            Button(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Profile", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.plantingDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updatePlantingDate(it) }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
