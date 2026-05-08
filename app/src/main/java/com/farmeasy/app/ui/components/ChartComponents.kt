package com.farmeasy.app.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.farmeasy.app.ui.theme.ForestGreen
import com.farmeasy.app.ui.theme.MossGreen
import com.farmeasy.app.ui.theme.WarmAmber
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChartDataPoint(
    val x: Float,
    val y: Float,
    val label: String = ""
)

@Composable
fun SensorLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = ForestGreen,
    fillColor: Color = MossGreen,
    chartHeight: Dp = 250.dp,
    label: String = "Value",
    yAxisLabel: String = "",
    showTimestamps: Boolean = true,
    animationDuration: Int = 800
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Chart styling
                description.isEnabled = false
                legend.isEnabled = true
                legend.textSize = 12f
                legend.textColor = AndroidColor.DKGRAY

                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)

                // X-Axis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textSize = 11f
                xAxis.textColor = AndroidColor.GRAY
                xAxis.granularity = 1f
                if (showTimestamps) {
                    xAxis.valueFormatter = object : ValueFormatter() {
                        private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            return try {
                                sdf.format(Date(value.toLong() * 1000))
                            } catch (e: Exception) {
                                ""
                            }
                        }
                    }
                }

                // Y-Axis (Left)
                axisLeft.textSize = 11f
                axisLeft.textColor = AndroidColor.GRAY
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = AndroidColor.parseColor("#E0E0E0")
                axisLeft.enableGridDashedLine(10f, 5f, 0f)

                // Y-Axis (Right)
                axisRight.isEnabled = false

                // Margins
                setExtraOffsets(8f, 8f, 8f, 8f)

                animateX(animationDuration)
            }
        },
        update = { chart ->
            if (data.isNotEmpty()) {
                val entries = data.map { Entry(it.x, it.y) }

                val dataSet = LineDataSet(entries, label).apply {
                    color = lineColor.toArgb()
                    setCircleColor(lineColor.toArgb())
                    circleRadius = 3f
                    lineWidth = 2.5f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    setFillColor(fillColor.toArgb())
                    fillAlpha = 40
                    setDrawValues(false)
                    setDrawCircleHole(false)
                    highLightColor = lineColor.toArgb()
                }

                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        }
    )
}

@Composable
fun DualLineChart(
    data1: List<ChartDataPoint>,
    data2: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    line1Color: Color = ForestGreen,
    line2Color: Color = WarmAmber,
    chartHeight: Dp = 250.dp,
    label1: String = "Current Season",
    label2: String = "Previous Season",
    showTimestamps: Boolean = false,
    animationDuration: Int = 800
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                description.isEnabled = false
                legend.isEnabled = true
                legend.textSize = 12f
                legend.textColor = AndroidColor.DKGRAY

                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.textSize = 11f
                xAxis.textColor = AndroidColor.GRAY
                xAxis.granularity = 1f
                if (!showTimestamps) {
                    xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return "W${value.toInt()}"
                        }
                    }
                }

                axisLeft.textSize = 11f
                axisLeft.textColor = AndroidColor.GRAY
                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = AndroidColor.parseColor("#E0E0E0")
                axisLeft.enableGridDashedLine(10f, 5f, 0f)
                axisRight.isEnabled = false

                setExtraOffsets(8f, 8f, 8f, 8f)
                animateX(animationDuration)
            }
        },
        update = { chart ->
            val dataSets = mutableListOf<ILineDataSet>()

            if (data1.isNotEmpty()) {
                val entries1 = data1.map { Entry(it.x, it.y) }
                val ds1 = LineDataSet(entries1, label1).apply {
                    color = line1Color.toArgb()
                    setCircleColor(line1Color.toArgb())
                    circleRadius = 3f
                    lineWidth = 2.5f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(true)
                    setFillColor(line1Color.toArgb())
                    fillAlpha = 30
                    setDrawValues(false)
                    setDrawCircleHole(false)
                }
                dataSets.add(ds1)
            }

            if (data2.isNotEmpty()) {
                val entries2 = data2.map { Entry(it.x, it.y) }
                val ds2 = LineDataSet(entries2, label2).apply {
                    color = line2Color.toArgb()
                    setCircleColor(line2Color.toArgb())
                    circleRadius = 3f
                    lineWidth = 2f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setDrawFilled(false)
                    setDrawValues(false)
                    setDrawCircleHole(false)
                    enableDashedLine(10f, 5f, 0f)
                }
                dataSets.add(ds2)
            }

            if (dataSets.isNotEmpty()) {
                chart.data = LineData(dataSets)
                chart.invalidate()
            }
        }
    )
}
