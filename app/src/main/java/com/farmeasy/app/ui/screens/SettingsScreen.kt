package com.farmeasy.app.ui.screens

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.millisToRelativeTime
import com.farmeasy.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToBluetooth: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineSmall) }
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
            // Language Selection
            SettingsSectionHeader(icon = Icons.Default.Language, title = "Language")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsViewModel.LANGUAGES.forEach { lang ->
                        SettingsOption(
                            label = lang,
                            isSelected = uiState.language == lang,
                            onClick = { viewModel.setLanguage(lang) }
                        )
                    }
                }
            }

            // Units
            SettingsSectionHeader(icon = Icons.Default.Thermostat, title = "Units")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Temperature", style = MaterialTheme.typography.bodyLarge)
                        Row {
                            SettingsViewModel.TEMP_UNITS.forEach { unit ->
                                TextButton(
                                    onClick = { viewModel.setTemperatureUnit(unit) }
                                ) {
                                    Text(
                                        text = unit,
                                        color = if (uiState.temperatureUnit == unit)
                                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (uiState.temperatureUnit == unit)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Area", style = MaterialTheme.typography.bodyLarge)
                        Row {
                            SettingsViewModel.AREA_UNITS.forEach { unit ->
                                TextButton(
                                    onClick = { viewModel.setAreaUnit(unit) }
                                ) {
                                    Text(
                                        text = unit,
                                        color = if (uiState.areaUnit == unit)
                                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (uiState.areaUnit == unit)
                                            FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Notification Preferences
            SettingsSectionHeader(icon = Icons.Default.Notifications, title = "Notifications")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    NotificationToggle("Weather Alerts", uiState.notifyWeather) { viewModel.setNotification("weather", it) }
                    NotificationToggle("Irrigation Updates", uiState.notifyIrrigation) { viewModel.setNotification("irrigation", it) }
                    NotificationToggle("Fertilizer Reminders", uiState.notifyFertilizer) { viewModel.setNotification("fertilizer", it) }
                    NotificationToggle("Yield Updates", uiState.notifyYield) { viewModel.setNotification("yield", it) }
                    NotificationToggle("System Health", uiState.notifySystem) { viewModel.setNotification("system", it) }
                    NotificationToggle("Market Prices", uiState.notifyMarket) { viewModel.setNotification("market", it) }
                }
            }

            // BLE Settings
            SettingsSectionHeader(icon = Icons.Default.Bluetooth, title = "Bluetooth")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Reconnect", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Automatically reconnect to last device",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.autoReconnect,
                            onCheckedChange = { viewModel.setAutoReconnect(it) }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    uiState.savedDeviceName?.let {
                        Text(
                            text = "Saved Device: $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onNavigateToBluetooth) {
                        Text("Manage Bluetooth Connection")
                    }
                }
            }

            // Cloud Sync
            SettingsSectionHeader(icon = Icons.Default.Cloud, title = "Cloud Sync")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.lastSyncTimestamp > 0) {
                        Text(
                            text = "Last sync: ${uiState.lastSyncTimestamp.millisToRelativeTime()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.triggerSync() }) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now")
                    }
                }
            }

            // Farm Profile
            SettingsSectionHeader(icon = Icons.Default.Agriculture, title = "Farm")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = onNavigateToProfile
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Edit Farm Profile", style = MaterialTheme.typography.bodyLarge)
                    Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            // About
            SettingsSectionHeader(icon = Icons.Default.Info, title = "About")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("FarmEasy v1.0.0", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Smart Farming Made Simple",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MIT ADT University, Pune",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Team: Meet Chapale, Pratham Bhosale, Yash Gaikwad, Abhishek Maurya",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SettingsOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
