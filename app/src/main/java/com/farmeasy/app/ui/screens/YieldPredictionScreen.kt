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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.ui.components.ChartDataPoint
import com.farmeasy.app.ui.components.DualLineChart
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.formatDecimal
import com.farmeasy.app.viewmodel.YieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YieldPredictionScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: YieldViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yield Prediction", style = MaterialTheme.typography.headlineSmall) },
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
            // Current Yield — Hero Number
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardGreenTint)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current Yield Projection",
                        style = MaterialTheme.typography.titleMedium,
                        color = ForestGreenDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = uiState.currentYield.formatDecimal(1),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "t/ha",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ForestGreenDark.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (uiState.trend) {
                                "up" -> Icons.Default.KeyboardArrowUp
                                "down" -> Icons.Default.KeyboardArrowDown
                                else -> Icons.Default.Remove
                            },
                            contentDescription = null,
                            tint = when (uiState.trend) {
                                "up" -> SuccessGreen
                                "down" -> ErrorRed
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "${if (uiState.trend == "up") "+" else ""}${uiState.trendPct.formatDecimal(1)}% this week",
                            style = MaterialTheme.typography.bodyLarge,
                            color = when (uiState.trend) {
                                "up" -> SuccessGreen
                                "down" -> ErrorRed
                                else -> Color.Gray
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Previous Season Comparison
                    uiState.previousSeasonYield?.let { prev ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Previous season: ${prev.formatDecimal(1)} t/ha",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestGreenDark.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Weekly Trend Chart
            Text(
                text = "Season Yield Trend",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val currentData = uiState.weeklyProjections.mapIndexed { index, wp ->
                        ChartDataPoint(x = (index + 1).toFloat(), y = wp.yieldEstimate)
                    }
                    // Simulated previous season
                    val prevData = currentData.map {
                        ChartDataPoint(x = it.x, y = (it.y * 0.91f))
                    }

                    DualLineChart(
                        data1 = currentData,
                        data2 = prevData,
                        line1Color = ForestGreen,
                        line2Color = WarmAmber,
                        label1 = "This Season",
                        label2 = "Previous Season",
                        chartHeight = 220.dp,
                        showTimestamps = false
                    )
                }
            }

            // Key Factors
            Text(
                text = "Key Factors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            uiState.keyFactors.forEach { factor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (factor.impact) {
                                "positive" -> "📈"
                                "negative" -> "📉"
                                else -> "➡️"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = factor.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = factor.value,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = when (factor.impact) {
                                        "positive" -> SuccessGreen
                                        "negative" -> ErrorRed
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Text(
                                text = factor.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // AI Insights
            if (uiState.aiInsight.isNotEmpty()) {
                Text(
                    text = "AI Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardAmberTint)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text(text = "🤖", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.aiInsight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmAmberDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
