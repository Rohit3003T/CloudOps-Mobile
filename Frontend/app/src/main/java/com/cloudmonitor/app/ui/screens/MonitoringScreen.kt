package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmonitor.app.data.model.MetricSeries
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

private fun formatBytes(v: Double?): String {
    if (v == null || v == 0.0) return "0 B"
    val kb = v / 1024.0
    return if (kb < 1024) "${"%.1f".format(kb)} KB" else "${"%.1f".format(kb / 1024)} MB"
}

@Composable
fun MonitoringScreen(viewModel: CloudMonitorViewModel) {
    val monState   by viewModel.monitoring.collectAsState()
    val instanceId by viewModel.monitoringInstanceId.collectAsState()
    val focus = LocalFocusManager.current

    var inputId  by remember { mutableStateOf(instanceId) }
    var expanded by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "CloudWatch Monitoring",
        actions = {
            IconButton(onClick = { viewModel.loadMonitoring(instanceId) }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Instance ID input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AwsDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputId,
                    onValueChange = { inputId = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("i-0abc123def456789a", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    label = { Text("Instance ID") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        focus.clearFocus()
                        viewModel.setMonitoringInstance(inputId.trim())
                        viewModel.loadMonitoring(inputId.trim())
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AwsOrange,
                        unfocusedBorderColor = BorderColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextSecondary,
                        cursorColor = AwsOrange,
                        focusedLabelColor = AwsOrange,
                        unfocusedLabelColor = TextTertiary,
                        focusedContainerColor = AwsDarkContainer,
                        unfocusedContainerColor = AwsDarkContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                )
                Button(
                    onClick = {
                        focus.clearFocus()
                        viewModel.setMonitoringInstance(inputId.trim())
                        viewModel.loadMonitoring(inputId.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AwsOrange, contentColor = AwsDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Query", fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = BorderColor)

            when (val s = monState) {
                is UiState.Loading -> LoadingState("Fetching CloudWatch metrics…")
                is UiState.Error   -> ErrorState(s.message, onRetry = { viewModel.loadMonitoring(instanceId) })
                is UiState.Success -> {
                    val m = s.data.metrics
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (s.isMock) MockBanner()

                        Text("Instance: $instanceId", fontSize = 12.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)

                        // CPU card
                        m?.cpuUtilization?.let { series ->
                            MetricCard(
                                title = "CPU Utilization",
                                series = series,
                                accentColor = AwsOrange,
                                unit = "%"
                            )
                        }

                        // Network In
                        m?.networkIn?.let { series ->
                            MetricCard(
                                title = "Network In",
                                series = series,
                                accentColor = Blue,
                                unit = "bytes",
                                format = ::formatBytes
                            )
                        }

                        // Network Out
                        m?.networkOut?.let { series ->
                            MetricCard(
                                title = "Network Out",
                                series = series,
                                accentColor = Purple,
                                unit = "bytes",
                                format = ::formatBytes
                            )
                        }

                        // Disk Read
                        m?.diskReadBytes?.let { series ->
                            MetricCard(title = "Disk Read", series = series, accentColor = Green, unit = "bytes", format = ::formatBytes)
                        }

                        // Disk Write
                        m?.diskWriteBytes?.let { series ->
                            MetricCard(title = "Disk Write", series = series, accentColor = Yellow, unit = "bytes", format = ::formatBytes)
                        }
                    }
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("◈", fontSize = 40.sp, color = TextTertiary)
                            Text("Enter an instance ID and tap Query", fontSize = 14.sp, color = TextTertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    series: MetricSeries,
    accentColor: androidx.compose.ui.graphics.Color,
    unit: String,
    format: ((Double?) -> String)? = null
) {
    val points = series.datapoints ?: emptyList()
    val values = points.mapNotNull { it.value }
    val latest = values.lastOrNull()
    val avg    = if (values.isNotEmpty()) values.average() else 0.0
    val max    = values.maxOrNull() ?: 0.0

    val display: (Double?) -> String = format ?: { v -> "${"%.2f".format(v ?: 0.0)} $unit" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                series.stat?.let { InfoChip(it) }
            }
            // Stats row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("LATEST", fontSize = 9.sp, color = TextTertiary, letterSpacing = 0.08.sp, fontWeight = FontWeight.Bold)
                    Text(display(latest), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                        color = accentColor, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("AVG", fontSize = 9.sp, color = TextTertiary, letterSpacing = 0.08.sp, fontWeight = FontWeight.Bold)
                    Text(display(avg), fontSize = 14.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("MAX", fontSize = 9.sp, color = TextTertiary, letterSpacing = 0.08.sp, fontWeight = FontWeight.Bold)
                    Text(display(max), fontSize = 14.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
            // Mini sparkline (progress bar approximation)
            if (values.isNotEmpty() && max > 0) {
                val normalized = (latest ?: 0.0) / max
                MetricBar(
                    label = "${points.size} datapoints",
                    value = display(latest),
                    progress = normalized.toFloat(),
                    color = accentColor
                )
            }
            Text("${points.size} datapoints · ${series.unit ?: unit}", fontSize = 10.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
        }
    }
}
