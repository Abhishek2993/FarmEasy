# FarmEasy 🌾

**Smart Farming Made Simple** — an Android app for sugarcane farmers that reads live field data from an ESP32 sensor node over Bluetooth Low Energy (BLE), controls irrigation valves, and brings together weather, yield predictions, alerts and market prices on one screen.

Built with **Kotlin**, **Jetpack Compose (Material 3)**, **Hilt**, **Room**, **Retrofit** and **WorkManager**. The UI is available in **English, Hindi (हिन्दी) and Marathi (मराठी)**.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [ESP32 / BLE protocol](#esp32--ble-protocol)
- [Backend API](#backend-api)
- [Local database](#local-database)
- [Background work](#background-work)
- [Permissions](#permissions)
- [Known gaps / roadmap](#known-gaps--roadmap)

---

## Features

| Screen | What it does |
|---|---|
| **Dashboard** | Live soil moisture, soil and air temperature, humidity, rainfall, valve state and node battery level, plus the current crop growth stage (worked out from the planting date). |
| **Bluetooth** | Scan for, connect to and disconnect from the FarmEasy ESP32 sensor node, with optional auto-reconnect to the last device. |
| **Sensor Data** | Detailed live readings with gauges and status indicators. |
| **Irrigation** | Per-zone valve status and the AI irrigation schedule. Manual open/close overrides are sent to the ESP32 over BLE and to the cloud. Every irrigation event is logged. |
| **Yield Prediction** | Projected yield (tonnes/hectare), comparison with last season, weekly projection chart, the factors driving the estimate and an AI insight. |
| **History** | Historical sensor readings and irrigation events, shown as charts (MPAndroidChart). |
| **Weather** | Current conditions, a 48-hour forecast, weather alerts and farming recommendations for the farm's location. |
| **Alerts** | Alerts grouped by category (weather, irrigation, fertilizer, yield, system, market) and severity (info, warning, critical), with read/unread state and links to the relevant screen. |
| **Farm Profile** | Farm name, location, area, sugarcane variety (Co-86032, CoM-0265, Co-92005, …), planting season (Suru / Adsali / Pre-seasonal), ratoon cycle, irrigation zones, water source and electricity window. |
| **Market Prices** | Sugarcane price per tonne for a region (Pune by default). |
| **Settings** | Language, temperature and area units, notification categories, alert thresholds and BLE auto-reconnect. |

**Works offline:** when the cloud API can't be reached, the repositories and ViewModels fall back to built-in demo data. This means you can run and explore the whole app with no backend and no hardware.

---

## Tech stack

| Area | Library | Version |
|---|---|---|
| Language | Kotlin | 2.1.0 |
| Build | Android Gradle Plugin / Gradle | 8.7.3 / 8.11.1 |
| UI | Jetpack Compose (BOM), Material 3, Material Icons Extended | 2024.12.01 |
| Navigation | Navigation Compose | 2.8.5 |
| DI | Hilt (+ Hilt Navigation Compose, Hilt Work) | 2.56.2 |
| Annotation processing | KSP | 2.1.0-1.0.29 |
| Database | Room | 2.6.1 |
| Networking | Retrofit + Gson, OkHttp + logging interceptor | 2.11.0 / 4.12.0 |
| Preferences | DataStore Preferences | 1.1.1 |
| Background jobs | WorkManager | 2.10.0 |
| Charts | MPAndroidChart (JitPack) | v3.1.0 |
| Async | Kotlin Coroutines / Flow | 1.9.0 |
| Splash | AndroidX Core SplashScreen | 1.0.1 |

**SDK levels:** `minSdk 26` (Android 8.0), `targetSdk` / `compileSdk 36`, Java/JVM target 17.

---

## Architecture

FarmEasy follows **MVVM** with a repository layer and Hilt for dependency injection.

```
┌──────────────────────────────────────────────────────────────┐
│  UI (Jetpack Compose screens + reusable components)          │
│        ▲  StateFlow<UiState>                │ user events    │
├────────┼────────────────────────────────────▼────────────────┤
│  ViewModels (@HiltViewModel)                                 │
├──────────────────────────────────────────────────────────────┤
│  Repositories: Sensor · Irrigation · Weather · Alert · Farm  │
├───────────────┬──────────────────┬─────────────┬─────────────┤
│  Room (local) │ Retrofit (cloud) │  DataStore  │  BleManager │
│  cache/history│ api.farmeasy.in  │  settings   │  ESP32 node │
└───────────────┴──────────────────┴─────────────┴─────────────┘
                 ▲
                 └── AlertWorker (WorkManager): sync, alerts, thresholds, cleanup
```

- **Single activity:** `MainActivity` hosts `AppNavigation`, a Compose `NavHost` with 12 routes. The bottom navigation bar appears on Dashboard, Irrigation, History, Alerts and Settings.
- **Reactive data:** the DAOs expose `Flow`s, and the ViewModels expose `StateFlow<…UiState>` that the screens collect.
- **BLE layer:** `BleManager` wraps scanning, GATT connection, notifications and valve writes, and publishes the connection state and parsed `SensorPacket`s as flows.

---

## Project structure

```
app/src/main/java/com/farmeasy/app/
├── FarmEasyApplication.kt      # @HiltAndroidApp, supplies HiltWorkerFactory to WorkManager
├── MainActivity.kt             # Splash screen + Compose entry point
├── bluetooth/
│   ├── BleManager.kt           # Scan / connect / notify / valve commands
│   └── BleDeviceModel.kt       # BleDeviceModel, SensorPacket, connection state
├── data/
│   ├── local/                  # Room: AppDatabase, entities, DAOs
│   ├── remote/                 # Retrofit ApiService + API models
│   └── repository/             # Alert, Farm, Irrigation, Sensor, Weather repositories
├── di/AppModule.kt             # Provides Room, OkHttp, Retrofit, ApiService, DataStore
├── navigation/AppNavigation.kt # Routes + NavHost + bottom navigation
├── ui/
│   ├── components/             # SensorCard, GaugeWidget, Charts, AlertBanner, StatusIndicator, BottomNavBar
│   ├── screens/                # One composable per screen (12 screens)
│   └── theme/                  # Colors, typography, Material 3 theme
├── utils/                      # Constants, extension functions (dates, crop stage, …)
├── viewmodel/                  # One ViewModel per feature
└── workers/AlertWorker.kt      # Periodic sync + local threshold alerts

app/src/main/res/
├── values/strings.xml          # English
├── values-hi/strings.xml       # Hindi
└── values-mr/strings.xml       # Marathi

app/schemas/                    # Exported Room schema (v1)
```

---

## Getting started

### Prerequisites

- **Android Studio** (a recent stable version that supports AGP 8.7 and compile SDK 36)
- **JDK 17**
- **Android SDK Platform 36**
- A physical Android device with **BLE** if you want to use the sensor hardware. The manifest marks `bluetooth_le` as *required*, so the app won't install on devices or emulators without BLE. See [Known gaps](#known-gaps--roadmap).

### Clone and build

```bash
git clone https://github.com/Abhishek2993/FarmEasy.git
```

```bash
cd FarmEasy
```

Open the folder in Android Studio and let Gradle sync. Or build from the command line:

```bash
./gradlew assembleDebug
```

Install on a connected device:

```bash
./gradlew installDebug
```

Debug builds use the application ID `com.farmeasy.app.debug`, so they can be installed next to a release build. Release builds turn on R8 minification and resource shrinking (`app/proguard-rules.pro`).

### Running without hardware or a backend

Just launch the app. If the API at `BASE_URL` can't be reached, the Weather, Yield, Dashboard, Market and other screens show realistic demo data.

---

## Configuration

All tunable values are in [`utils/Constants.kt`](app/src/main/java/com/farmeasy/app/utils/Constants.kt):

| Constant | Default | Purpose |
|---|---|---|
| `BASE_URL` | `https://api.farmeasy.in/v1/` | Cloud API base URL |
| `API_TIMEOUT_SECONDS` | `30` | OkHttp timeouts |
| `BLE_SERVICE_UUID` / `*_CHAR_UUID` | see [BLE protocol](#esp32--ble-protocol) | ESP32 GATT service and characteristics |
| `BLE_SCAN_TIMEOUT_MS` | `15000` | How long a BLE scan runs |
| `MOISTURE_CRITICAL_LOW` / `LOW` / `OPTIMAL_HIGH` / `HIGH` | `20 / 40 / 70 / 75` % | Soil moisture bands |
| `TEMP_COLD_STRESS` / `OPTIMAL_HIGH` / `HEAT_STRESS` | `15 / 35 / 40` °C | Temperature bands |
| `RAINFALL_HEAVY_THRESHOLD` | `30` mm | Heavy rain threshold |
| `FORMATIVE_PHASE_END` / `GRAND_GROWTH_PHASE_END` | `120 / 270` days | Sugarcane crop stage boundaries |
| `SYNC_INTERVAL_HOURS` | `1` | Intended background sync interval |
| `DATA_RETENTION_DAYS` | `90` | Local sensor data retention |
| `DEFAULT_LATITUDE` / `DEFAULT_LONGITUDE` | `18.5204, 73.8567` | Default farm location (Pune, Maharashtra) |
| `DEFAULT_FARM_ID` / `DEFAULT_NODE_ID` | `1` / `1` | Farm and sensor node IDs |

User preferences (language, units, thresholds, notification toggles, last BLE device, farm profile, …) are saved with **DataStore Preferences**.

---

## ESP32 / BLE protocol

The app talks to a custom GATT service on the FarmEasy ESP32 node:

| Item | UUID |
|---|---|
| Service | `4fafc201-1fb5-459e-8fcc-c5c9c331914b` |
| Sensor data characteristic (notify / read) | `beb5483e-36e1-4688-b7f5-ea07361b26a8` |
| Valve command characteristic (write) | `beb5483e-36e1-4688-b7f5-ea07361b26a9` |

### Sensor data (ESP32 → app)

After services are discovered, the app subscribes to notifications on the sensor characteristic. It can also request a reading on demand. The preferred payload is **JSON**:

```json
{
  "nodeId": 1,
  "soilMoisture": 45.2,
  "soilTemperature": 28.4,
  "ambientTemp": 32.1,
  "humidity": 61.0,
  "rainfallMm": 0.0,
  "valveStatus": false,
  "batteryPct": 87,
  "timestamp": 1735689600
}
```

If JSON parsing fails, the app falls back to **CSV** in this order:

```
nodeId,soilMoisture,soilTemperature,ambientTemp,humidity,rainfallMm,valveStatus,batteryPct[,timestamp]
```

If `timestamp` (Unix seconds) is left out, the app uses the phone's current time.

### Valve command (app → ESP32)

Written as UTF-8 JSON to the valve command characteristic:

```json
{ "zone": 1, "action": "open" }
```

`action` is `"open"` or `"close"`.

---

## Backend API

The Retrofit interface is [`data/remote/ApiService.kt`](app/src/main/java/com/farmeasy/app/data/remote/ApiService.kt). All paths are relative to `BASE_URL`.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `sensor-data/latest` | Latest sensor reading |
| `GET` | `sensor-data/history` | Historical readings |
| `POST` | `sensor-data/sync` | Upload locally stored readings |
| `GET` | `yield/prediction` | AI yield prediction for a farm |
| `GET` | `irrigation/schedule` | Irrigation schedule and zone status |
| `POST` | `irrigation/override` | Manual valve override |
| `GET` | `weather/current` | Current weather for lat/lon |
| `GET` | `weather/forecast` | Hourly forecast and weather alerts |
| `GET` | `alerts` | Farm alerts |
| `POST` | `farm/profile` | Save the farm profile |
| `GET` | `market/prices?region=` | Market prices for a region |

Request and response models are in `data/remote/ApiModels.kt`.

---

## Local database

Room database `AppDatabase` (schema v1, exported to `app/schemas/`). It is built with `fallbackToDestructiveMigration()`.

| Table | Entity | Key fields |
|---|---|---|
| `sensor_readings` | `SensorReadingEntity` | nodeId, soilMoisture, soilTemperature, ambientTemp, humidity, rainfallMm, valveStatus, batteryPct, timestamp, receivedAt, isSynced |
| `alerts` | `AlertEntity` | category, title, description, iconType, severity, actionLabel, actionRoute, isRead, timestamp, sourceId |
| `irrigation_events` | `IrrigationEventEntity` | zone, startTime, endTime, durationMinutes, waterVolumeLiters, isAiAutomated, reason |

---

## Background work

`AlertWorker` is a `@HiltWorker` `CoroutineWorker`. Each time it runs, it:

1. Uploads sensor readings that haven't been synced to the cloud yet
2. Fetches new alerts from the cloud
3. Checks the latest local reading against thresholds and posts notifications for critical low moisture, heat stress and low node battery
4. Deletes sensor data older than `DATA_RETENTION_DAYS`

It retries up to 3 times on failure. Notifications use three channels: **Farm Alerts**, **Irrigation Updates** and **System Health**.

---

## Permissions

| Permission | Why |
|---|---|
| `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (and legacy `BLUETOOTH`, `BLUETOOTH_ADMIN` up to API 30) | Find and talk to the ESP32 node |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | Needed for BLE scanning on Android 11 and below |
| `INTERNET`, `ACCESS_NETWORK_STATE` | Cloud API |
| `POST_NOTIFICATIONS` | Farm alerts (Android 13+) |
| `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_CONNECTED_DEVICE` | Keep the BLE connection alive |
| `WAKE_LOCK` | WorkManager |

---

## Known gaps / roadmap

I found these while reviewing the current code. They are worth knowing about before you depend on these features:

- **`AlertWorker` is never scheduled.** The worker and the Hilt `WorkerFactory` are set up, but no code calls `enqueue` on a `PeriodicWorkRequest`. Background sync and threshold notifications won't run until something schedules it, for example in `FarmEasyApplication.onCreate()` using `SYNC_INTERVAL_HOURS`.
- **Choosing a language doesn't change the UI language yet.** The Settings screen saves English / हिन्दी / मराठी to DataStore, and `values-hi` / `values-mr` strings exist, but the app never applies the chosen locale (for example with `AppCompatDelegate.setApplicationLocales`). Today the language follows the device's system language.
- **BLE is a hard install requirement.** `android.hardware.bluetooth_le` is declared `required="true"`. Changing it to `false` would let the app's demo mode run on emulators and devices without BLE.
- **The database is wiped on schema changes** (`fallbackToDestructiveMigration`). Add real migrations before shipping schema v2.
- **No tests yet.** There is no `src/test` or `src/androidTest` source set.
- **The farm and node IDs are hardcoded** (`DEFAULT_FARM_ID`, `DEFAULT_NODE_ID`), so only one farm and one node are supported.

---

## Contributing

1. Fork the repository and create a feature branch.
2. Follow the official Kotlin code style (`kotlin.code.style=official`).
3. Make sure `./gradlew assembleDebug` passes, then open a pull request.

## Author

**Abhishek Maurya** ([@Abhishek2993](https://github.com/Abhishek2993))
