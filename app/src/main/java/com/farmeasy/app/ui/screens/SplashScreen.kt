package com.farmeasy.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmeasy.app.ui.theme.SplashGradientEnd
import com.farmeasy.app.ui.theme.SplashGradientMid
import com.farmeasy.app.ui.theme.SplashGradientStart
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToDashboard: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "alpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 800),
        label = "scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SplashGradientStart,
                        SplashGradientMid,
                        SplashGradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
        ) {
            // Leaf/Plant Icon
            Icon(
                imageVector = Icons.Default.Eco,
                contentDescription = "FarmEasy Logo",
                tint = Color.White,
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FarmEasy",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Smart Farming Made Simple",
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Powered by text
            Text(
                text = "MIT ADT University, Pune",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
