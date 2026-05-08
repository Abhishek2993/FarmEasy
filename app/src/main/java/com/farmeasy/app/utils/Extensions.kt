package com.farmeasy.app.utils

import androidx.compose.ui.graphics.Color
import com.farmeasy.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ---- Date/Time Formatting ----

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this * 1000))
}

fun Long.toFormattedTime(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(this * 1000))
}

fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(this * 1000))
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diffMs = now - (this * 1000)
    val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "${diffMinutes}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays < 7 -> "${diffDays}d ago"
        else -> toFormattedDate()
    }
}

fun Long.millisToRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diffMs = now - this
    val diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)

    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "${diffMinutes}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays < 7 -> "${diffDays}d ago"
        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(this))
        }
    }
}

// ---- Sensor Status Colors ----

fun getMoistureColor(value: Float): Color {
    return when {
        value < Constants.MOISTURE_CRITICAL_LOW -> MoistureCriticalDry
        value < Constants.MOISTURE_LOW -> MoistureLow
        value <= Constants.MOISTURE_OPTIMAL_HIGH -> MoistureOptimal
        else -> MoistureWaterlogged
    }
}

fun getMoistureLabel(value: Float): String {
    return when {
        value < Constants.MOISTURE_CRITICAL_LOW -> "Critical Dry"
        value < Constants.MOISTURE_LOW -> "Low"
        value <= Constants.MOISTURE_OPTIMAL_HIGH -> "Optimal"
        else -> "Waterlogged Risk"
    }
}

fun getTemperatureColor(value: Float): Color {
    return when {
        value < Constants.TEMP_COLD_STRESS -> TempColdStress
        value <= Constants.TEMP_OPTIMAL_HIGH -> TempOptimal
        else -> TempHeatStress
    }
}

fun getTemperatureLabel(value: Float): String {
    return when {
        value < Constants.TEMP_COLD_STRESS -> "Cold Stress"
        value <= Constants.TEMP_OPTIMAL_HIGH -> "Optimal"
        else -> "Heat Stress"
    }
}

fun getRainfallLabel(rainfallMm: Float): String {
    return when {
        rainfallMm <= 0f -> "No Rain"
        rainfallMm < 10f -> "Light Rain"
        rainfallMm < Constants.RAINFALL_HEAVY_THRESHOLD -> "Moderate Rain"
        else -> "Heavy Rain"
    }
}

// ---- Crop Stage ----

fun getCropStage(plantingDateMillis: Long): String {
    val daysSincePlanting = getDaysSincePlanting(plantingDateMillis)
    return when {
        daysSincePlanting <= Constants.FORMATIVE_PHASE_END -> "Formative Phase"
        daysSincePlanting <= Constants.GRAND_GROWTH_PHASE_END -> "Grand Growth Phase"
        else -> "Maturation Phase"
    }
}

fun getCropStageProgress(plantingDateMillis: Long): Float {
    val daysSincePlanting = getDaysSincePlanting(plantingDateMillis)
    return (daysSincePlanting / 365f).coerceIn(0f, 1f)
}

fun getDaysSincePlanting(plantingDateMillis: Long): Int {
    val now = System.currentTimeMillis()
    return TimeUnit.MILLISECONDS.toDays(now - plantingDateMillis).toInt()
}

// ---- Unit Conversion ----

fun Float.celsiusToFahrenheit(): Float = (this * 9f / 5f) + 32f
fun Float.fahrenheitToCelsius(): Float = (this - 32f) * 5f / 9f
fun Float.acresToHectares(): Float = this * 0.4047f
fun Float.hectaresToAcres(): Float = this / 0.4047f

// ---- Helper Extensions ----

fun Float.formatDecimal(places: Int = 1): String = "%.${places}f".format(this)

fun getStartOfDay(daysAgo: Int = 0): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis / 1000 // Unix timestamp in seconds
}

fun getStartOfDayMillis(daysAgo: Int = 0): Long {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
