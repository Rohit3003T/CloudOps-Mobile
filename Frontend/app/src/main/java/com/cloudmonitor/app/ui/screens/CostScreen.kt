package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

private val BAR_COLORS = listOf(AwsOrange, Blue, Green, Purple, Yellow, Red,
    Color(0xFF00D2FF), Color(0xFF7BED9F), Color(0xFFECCC68), Color(0xFFFF6B81))

@Composable
fun CostScreen(viewModel: CloudMonitorViewModel) {
    val costState by viewModel.cost.collectAsState()

    LaunchedEffect(Unit) {
        if (costState is UiState.Idle) viewModel.loadCost()
    }

    ScreenScaffold(
        title = "Cost Explorer",
        actions = {
            IconButton(onClick = { viewModel.loadCost() }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        when (val s = costState) {
            is UiState.Loading -> LoadingState("Fetching cost data…")
            is UiState.Error   -> ErrorState(s.message, onRetry = { viewModel.loadCost() })
            is UiState.Success -> {
                val cost = s.data
                val total = cost.totalCost?.toDoubleOrNull() ?: 0.0

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        if (s.isMock) MockBanner()
                    }

                    // Total cost hero card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Month-to-Date Cost", fontSize = 12.sp, color = TextTertiary)
                                        Text(
                                            "$${"%.2f".format(total)}",
                                            fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary, fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(cost.currency ?: "USD", fontSize = 13.sp, color = TextTertiary)
                                        Text(
                                            "${cost.serviceCount ?: cost.services.size} services",
                                            fontSize = 12.sp, color = TextSecondary
                                        )
                                    }
                                }
                                cost.period?.let { period ->
                                    Text(
                                        "${period.start} → ${period.end}",
                                        fontSize = 11.sp, color = TextTertiary, fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    item { SectionHeader("By Service") }

                    itemsIndexed(cost.services) { index, svc ->
                        val amount = svc.amount.toDoubleOrNull() ?: 0.0
                        val pct    = if (total > 0) (amount / total).toFloat() else 0f
                        val color  = BAR_COLORS[index % BAR_COLORS.size]

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(color, RoundedCornerShape(2.dp))
                                        )
                                        Text(
                                            svc.service.replace("Amazon ", "").replace("AWS ", ""),
                                            fontSize = 13.sp, color = TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1, modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Text(
                                        "$${"%.4f".format(amount)}",
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                        color = color, fontFamily = FontFamily.Monospace
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth().height(5.dp).then(
                                        Modifier.background(AwsDarkContainer, RoundedCornerShape(3.dp))
                                    ),
                                    color = color,
                                    trackColor = AwsDarkContainer
                                )
                                Text(
                                    "${"%.1f".format(pct * 100)}% of total",
                                    fontSize = 10.sp, color = TextTertiary
                                )
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
