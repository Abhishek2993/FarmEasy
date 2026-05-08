package com.farmeasy.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.farmeasy.app.ui.components.BottomNavBar
import com.farmeasy.app.ui.screens.*
import com.farmeasy.app.viewmodel.AlertsViewModel

object Routes {
    const val SPLASH = "splash"
    const val DASHBOARD = "dashboard"
    const val BLUETOOTH = "bluetooth"
    const val SENSOR_DATA = "sensor_data"
    const val IRRIGATION = "irrigation"
    const val YIELD = "yield_prediction"
    const val HISTORY = "history"
    const val WEATHER = "weather"
    const val ALERTS = "alerts"
    const val FARM_PROFILE = "farm_profile"
    const val MARKET_PRICE = "market_price"
    const val SETTINGS = "settings"
}

// Screens that show the bottom navigation bar
private val bottomNavScreens = setOf(
    Routes.DASHBOARD,
    Routes.IRRIGATION,
    Routes.HISTORY,
    Routes.ALERTS,
    Routes.SETTINGS
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.SPLASH
    val showBottomNav = currentRoute in bottomNavScreens

    // Get unread alert count for badge
    var unreadAlertCount by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    unreadAlertCount = unreadAlertCount,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to dashboard to avoid building up a large back stack
                            popUpTo(Routes.DASHBOARD) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = if (showBottomNav) Modifier.padding(padding) else Modifier
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToDashboard = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNavigateToBluetooth = { navController.navigate(Routes.BLUETOOTH) },
                    onNavigateToYield = { navController.navigate(Routes.YIELD) },
                    onNavigateToIrrigation = {
                        navController.navigate(Routes.IRRIGATION) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToWeather = { navController.navigate(Routes.WEATHER) },
                    onNavigateToSensorData = { navController.navigate(Routes.SENSOR_DATA) }
                )
            }

            composable(Routes.BLUETOOTH) {
                BluetoothScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SENSOR_DATA) {
                SensorDataScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.IRRIGATION) {
                IrrigationScreen()
            }

            composable(Routes.YIELD) {
                YieldPredictionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HISTORY) {
                HistoryScreen()
            }

            composable(Routes.WEATHER) {
                WeatherScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ALERTS) {
                val alertsViewModel: AlertsViewModel = hiltViewModel()
                val count by alertsViewModel.unreadCount.collectAsState()
                unreadAlertCount = count

                AlertsScreen(
                    onNavigate = { route -> navController.navigate(route) },
                    viewModel = alertsViewModel
                )
            }

            composable(Routes.FARM_PROFILE) {
                FarmProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.MARKET_PRICE) {
                MarketPriceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateToProfile = { navController.navigate(Routes.FARM_PROFILE) },
                    onNavigateToBluetooth = { navController.navigate(Routes.BLUETOOTH) }
                )
            }
        }
    }
}
