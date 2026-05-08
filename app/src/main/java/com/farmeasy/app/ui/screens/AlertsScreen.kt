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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.data.local.AlertEntity
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.millisToRelativeTime
import com.farmeasy.app.viewmodel.AlertsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val alerts by viewModel.allAlerts.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    val filteredAlerts = if (uiState.selectedCategory != null) {
        alerts.filter { it.category == uiState.selectedCategory }
    } else {
        alerts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Alerts", style = MaterialTheme.typography.headlineSmall)
                        if (unreadCount > 0) {
                            Text(
                                text = "$unreadCount unread",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmAmber
                            )
                        }
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Read All")
                        }
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("All") }
                        )
                    }
                    items(AlertsViewModel.CATEGORIES) { (key, label) ->
                        FilterChip(
                            selected = uiState.selectedCategory == key,
                            onClick = { viewModel.selectCategory(key) },
                            label = { Text(label) }
                        )
                    }
                }
            }

            if (filteredAlerts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔔", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No alerts",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(filteredAlerts) { alert ->
                AlertListItem(
                    alert = alert,
                    onClick = {
                        viewModel.markAsRead(alert.id)
                        alert.actionRoute?.let { onNavigate(it) }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun AlertListItem(
    alert: AlertEntity,
    onClick: () -> Unit
) {
    val icon = getAlertIcon(alert.category)
    val bgColor = when {
        !alert.isRead && alert.severity == "critical" -> CardRedTint
        !alert.isRead && alert.severity == "warning" -> CardAmberTint
        !alert.isRead -> CardGreenTint
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!alert.isRead) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when (alert.severity) {
                    "critical" -> ErrorRed
                    "warning" -> WarmAmber
                    else -> ForestGreen
                },
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (!alert.isRead) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = alert.timestamp.millisToRelativeTime(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                alert.actionLabel?.let { label ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun getAlertIcon(category: String): ImageVector {
    return when (category) {
        "weather" -> Icons.Default.Cloud
        "irrigation" -> Icons.Default.WaterDrop
        "fertilizer" -> Icons.Default.Eco
        "yield" -> Icons.Default.TrendingUp
        "system" -> Icons.Default.BatteryAlert
        "market" -> Icons.Default.AttachMoney
        else -> Icons.Default.Agriculture
    }
}
