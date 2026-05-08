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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.farmeasy.app.data.remote.MarketPriceResponse
import com.farmeasy.app.data.repository.FarmRepository
import com.farmeasy.app.ui.components.ChartDataPoint
import com.farmeasy.app.ui.components.SensorLineChart
import com.farmeasy.app.ui.theme.*
import com.farmeasy.app.utils.formatDecimal
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPriceScreen(
    onNavigateBack: () -> Unit = {},
    farmRepository: FarmRepository? = null
) {
    // Use demo data since this is an optional screen
    var marketData by remember {
        mutableStateOf<MarketPriceResponse?>(null)
    }

    LaunchedEffect(Unit) {
        // Load demo data directly
        val demoData = MarketPriceResponse(
            region = "Pune",
            currentPrice = 3150f,
            priceTrend = (0..29).map { day ->
                com.farmeasy.app.data.remote.PricePoint(
                    date = "Day ${30 - day}",
                    price = 3000f + (kotlin.math.sin(day * 0.2) * 150).toFloat()
                )
            },
            recommendation = "Prices are trending upward. Current maturity level suggests harvesting in 2-3 weeks would optimize returns.",
            lastUpdated = System.currentTimeMillis() / 1000
        )
        marketData = demoData
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Prices", style = MaterialTheme.typography.headlineSmall) },
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
            marketData?.let { data ->
                // Current Price
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
                            text = "Current Sugarcane Price",
                            style = MaterialTheme.typography.titleMedium,
                            color = ForestGreenDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹",
                                style = MaterialTheme.typography.headlineMedium,
                                color = ForestGreen,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = data.currentPrice.formatDecimal(0),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                        Text(
                            text = "per tonne • ${data.region}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ForestGreenDark.copy(alpha = 0.7f)
                        )
                    }
                }

                // Price Trend Chart
                Text(
                    text = "30-Day Price Trend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        val chartData = data.priceTrend.mapIndexed { index, point ->
                            ChartDataPoint(x = index.toFloat(), y = point.price)
                        }
                        SensorLineChart(
                            data = chartData,
                            lineColor = ForestGreen,
                            fillColor = MossGreen,
                            label = "Price (₹/tonne)",
                            chartHeight = 220.dp,
                            showTimestamps = false
                        )
                    }
                }

                // AI Recommendation
                data.recommendation?.let { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardAmberTint)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Text(text = "📊", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Harvest Timing Suggestion",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmAmberDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rec,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmAmberDark
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
