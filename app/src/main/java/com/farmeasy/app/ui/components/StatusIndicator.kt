package com.farmeasy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusIndicator(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Int = 10
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(dotSize.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ConnectionStatusIndicator(
    isConnected: Boolean,
    deviceName: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = if (isConnected) Color(0x1A4CAF50) else Color(0x1AD32F2F),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isConnected) Color(0xFF4CAF50) else Color(0xFFD32F2F))
        )
        Text(
            text = if (isConnected) "Connected${deviceName?.let { " • $it" } ?: ""}" else "Disconnected",
            style = MaterialTheme.typography.labelMedium,
            color = if (isConnected) Color(0xFF2E7D32) else Color(0xFFB71C1C)
        )
    }
}
