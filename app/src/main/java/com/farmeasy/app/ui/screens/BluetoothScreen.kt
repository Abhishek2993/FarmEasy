package com.farmeasy.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularAlt1Bar
import androidx.compose.material.icons.filled.SignalCellularAlt2Bar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.bluetooth.BleConnectionState
import com.farmeasy.app.bluetooth.BleDeviceModel
import com.farmeasy.app.bluetooth.SignalStrength
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.viewmodel.BluetoothViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BluetoothViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Bluetooth Connection", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleHelp() }) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, "Help")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Help Guide
            item {
                AnimatedVisibility(visible = uiState.showHelp) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBlueTint)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📡 Setup Guide",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "1. Make sure your ESP32 sensor device is powered on\n" +
                                        "2. Keep your phone within 10 meters of the device\n" +
                                        "3. Tap 'Scan for Devices' below\n" +
                                        "4. Select your FarmEasy device from the list\n" +
                                        "5. Wait for the connection to establish\n\n" +
                                        "💡 The ESP32 device name usually starts with 'FarmEasy' or 'ESP32'",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Connection Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (uiState.connectionState) {
                            BleConnectionState.CONNECTED -> CardGreenTint
                            BleConnectionState.ERROR -> CardRedTint
                            BleConnectionState.SCANNING, BleConnectionState.CONNECTING -> CardAmberTint
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (uiState.connectionState) {
                                BleConnectionState.CONNECTED -> Icons.Default.BluetoothConnected
                                BleConnectionState.SCANNING -> Icons.Default.BluetoothSearching
                                BleConnectionState.DISCONNECTED -> Icons.Default.LinkOff
                                else -> Icons.Default.Bluetooth
                            },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = when (uiState.connectionState) {
                                BleConnectionState.CONNECTED -> SuccessGreen
                                BleConnectionState.ERROR -> ErrorRed
                                BleConnectionState.SCANNING -> WarmAmber
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Status: ${uiState.connectionState.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            uiState.connectedDevice?.let {
                                Text(
                                    text = "${it.displayName} (${it.address})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (uiState.connectionState == BleConnectionState.SCANNING ||
                            uiState.connectionState == BleConnectionState.CONNECTING
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }

            // Reconnect Last Device
            item {
                if (uiState.lastDeviceAddress != null &&
                    uiState.connectionState != BleConnectionState.CONNECTED
                ) {
                    OutlinedButton(
                        onClick = { viewModel.reconnectLastDevice() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reconnect to ${uiState.lastDeviceName ?: "Last Device"}")
                    }
                }
            }

            // Scan / Stop Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.connectionState == BleConnectionState.SCANNING) {
                        Button(
                            onClick = { viewModel.stopScan() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("Stop Scan", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.startScan() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.connectionState != BleConnectionState.CONNECTING
                        ) {
                            Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan for Devices", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    if (uiState.connectionState == BleConnectionState.CONNECTED) {
                        OutlinedButton(
                            onClick = { viewModel.disconnect() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Disconnect", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // Discovered Devices
            if (uiState.discoveredDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "Found Devices (${uiState.discoveredDevices.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(uiState.discoveredDevices) { device ->
                    DeviceListItem(
                        device = device,
                        onConnect = { viewModel.connectToDevice(device) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DeviceListItem(
    device: BleDeviceModel,
    onConnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onConnect
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
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = device.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = device.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = when (device.signalStrength) {
                    SignalStrength.STRONG -> Icons.Default.SignalCellular4Bar
                    SignalStrength.MEDIUM -> Icons.Default.SignalCellularAlt
                    SignalStrength.WEAK -> Icons.Default.SignalCellularAlt2Bar
                    SignalStrength.VERY_WEAK -> Icons.Default.SignalCellularAlt1Bar
                },
                contentDescription = "Signal: ${device.signalStrength}",
                tint = when (device.signalStrength) {
                    SignalStrength.STRONG -> SuccessGreen
                    SignalStrength.MEDIUM -> MossGreen
                    SignalStrength.WEAK -> WarmAmber
                    SignalStrength.VERY_WEAK -> ErrorRed
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
