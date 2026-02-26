package com.cloudops.mobile.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudops.mobile.ui.components.*
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun CloudWatchScreen(
    viewModel: MainViewModel,
    instanceId: String,
    onBack: () -> Unit
) {
    val state by viewModel.cpuMetrics.collectAsState()

    LaunchedEffect(instanceId) { viewModel.fetchCpuMetrics(instanceId) }

    Scaffold(
        topBar = {
            CloudOpsTopBar("CloudWatch Metrics", onBack = onBack, actions = {
                IconButton(onClick = { viewModel.fetchCpuMetrics(instanceId) }) {
                    Icon(Icons.Default.Refresh, null, tint = AwsLightText)
                }
            })
        },
        containerColor = AwsDark
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen()
            state.error != null -> Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
                ErrorCard(state.error!!) { viewModel.fetchCpuMetrics(instanceId) }
            }
            else -> {
                val datapoints = state.data?.datapoints ?: emptyList()
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AwsDarkCard),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Computer, null, tint = AwsOrange, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Instance ID", fontSize = 11.sp, color = AwsGray)
                                    Text(instanceId, fontSize = 14.sp, color = AwsLightText, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    item { SectionHeader("CPU Utilization — Last 3 Hours") }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (datapoints.isEmpty()) {
                                    Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.BarChart, null, tint = AwsGray, modifier = Modifier.size(40.dp))
                                            Spacer(Modifier.height(8.dp))
                                            Text("No data available for this period", color = AwsGray, fontSize = 13.sp)
                                        }
                                    }
                                } else {
                                    // Simple line chart
                                    val maxY = datapoints.maxOf { it.maximum }.coerceAtLeast(10.0)
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    ) {
                                        val w = size.width
                                        val h = size.height
                                        val pts = datapoints.size
                                        if (pts < 2) return@Canvas

                                        fun xFor(i: Int) = i * (w / (pts - 1).toFloat())
                                        fun yFor(v: Double) = h - (v / maxY * h).toFloat()

                                        // Average line
                                        val avgPath = Path()
                                        datapoints.forEachIndexed { i, dp ->
                                            val x = xFor(i); val y = yFor(dp.average)
                                            if (i == 0) avgPath.moveTo(x, y) else avgPath.lineTo(x, y)
                                        }
                                        drawPath(avgPath, AwsOrange, style = Stroke(width = 3.dp.toPx()))

                                        // Max line
                                        val maxPath = Path()
                                        datapoints.forEachIndexed { i, dp ->
                                            val x = xFor(i); val y = yFor(dp.maximum)
                                            if (i == 0) maxPath.moveTo(x, y) else maxPath.lineTo(x, y)
                                        }
                                        drawPath(maxPath, AwsRed.copy(alpha = 0.6f), style = Stroke(width = 2.dp.toPx()))

                                        // Dots
                                        datapoints.forEachIndexed { i, dp ->
                                            drawCircle(AwsOrange, 4.dp.toPx(), Offset(xFor(i), yFor(dp.average)))
                                        }
                                    }

                                    // Legend
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Canvas(Modifier.size(12.dp, 3.dp)) { drawRect(AwsOrange) }
                                            Text("Average", fontSize = 12.sp, color = AwsGray)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Canvas(Modifier.size(12.dp, 3.dp)) { drawRect(AwsRed.copy(0.6f)) }
                                            Text("Maximum", fontSize = 12.sp, color = AwsGray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stats summary
                    if (datapoints.isNotEmpty()) {
                        item {
                            SectionHeader("Statistics")
                            val avgCpu = datapoints.map { it.average }.average()
                            val maxCpu = datapoints.maxOf { it.maximum }
                            val minCpu = datapoints.minOf { it.average }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard("Avg CPU", "${String.format("%.1f", avgCpu)}%", Icons.Default.Speed, AwsOrange, Modifier.weight(1f))
                                StatCard("Max CPU", "${String.format("%.1f", maxCpu)}%", Icons.Default.TrendingUp, AwsRed, Modifier.weight(1f))
                                StatCard("Min CPU", "${String.format("%.1f", minCpu)}%", Icons.Default.TrendingDown, AwsGreen, Modifier.weight(1f))
                            }
                        }

                        item {
                            SectionHeader("Data Points (${datapoints.size})")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
                            ) {
                                Column {
                                    datapoints.takeLast(10).reversed().forEachIndexed { idx, dp ->
                                        if (idx > 0) HorizontalDivider(color = AwsDark)
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(dp.timestamp.substring(11, 16), color = AwsGray, fontSize = 13.sp)
                                            Text("Avg: ${dp.average}%", color = AwsLightText, fontSize = 13.sp)
                                            Text("Max: ${dp.maximum}%", color = AwsRed, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}
