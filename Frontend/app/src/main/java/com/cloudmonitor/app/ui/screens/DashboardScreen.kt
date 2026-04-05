package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun DashboardScreen(viewModel: CloudMonitorViewModel) {
    val healthState by viewModel.health.collectAsState()
    val costState   by viewModel.cost.collectAsState()
    val ec2State    by viewModel.ec2.collectAsState()
    val s3State     by viewModel.s3.collectAsState()
    val username    by viewModel.username.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHealth()
        viewModel.loadCost()
        viewModel.loadEc2()
        viewModel.loadS3()
    }

    ScreenScaffold(
        title = "Dashboard",
        actions = {
            IconButton(onClick = {
                viewModel.loadHealth(); viewModel.loadCost()
                viewModel.loadEc2(); viewModel.loadS3()
            }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AwsDarkContainer, RoundedCornerShape(10.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AwsOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (username?.firstOrNull() ?: 'U').uppercaseChar().toString(),
                        fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 16.sp
                    )
                }
                Column {
                    Text("Welcome back, ${username ?: "User"}", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("CloudOps", fontSize = 12.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.Default.Logout, "Logout", tint = TextTertiary, modifier = Modifier.size(20.dp))
                }
            }

            // Health section
            SectionHeader("Backend Status")
            when (val s = healthState) {
                is UiState.Loading -> ShimmerItem(modifier = Modifier.fillMaxWidth().height(100.dp))
                is UiState.Success -> {
                    val h = s.data
                    val isOk = h.status == "OK"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isOk) Color(0x1F22D87A) else Color(0x1FFF4757),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (isOk) Green else Red, CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isOk) "API Online" else "API Offline",
                                fontWeight = FontWeight.Bold,
                                color = if (isOk) Green else Red
                            )
                            Text(
                                "v${h.version ?: "?"} · ${h.uptime ?: "?"} · ${h.aws?.region ?: "?"}",
                                fontSize = 12.sp, color = TextTertiary, fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(
                                    if (h.aws?.credentialsConfigured == true) Green else Yellow, CircleShape))
                                Text(if (h.aws?.credentialsConfigured == true) "AWS" else "Demo", fontSize = 11.sp, color = TextTertiary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(
                                    if (h.github?.tokenConfigured == true) Green else Yellow, CircleShape))
                                Text(if (h.github?.tokenConfigured == true) "GitHub" else "No Token", fontSize = 11.sp, color = TextTertiary)
                            }
                        }
                    }
                }
                is UiState.Error -> Text(s.message, color = Red, fontSize = 12.sp)
                else -> {}
            }

            // Stats grid
            SectionHeader("Overview")
            val ec2Count   = (ec2State as? UiState.Success)?.data?.count ?: 0
            val ec2Running = (ec2State as? UiState.Success)?.data?.instances?.count { it.state == "running" } ?: 0
            val s3Count    = (s3State as? UiState.Success)?.data?.count  ?: 0
            val totalCost  = (costState as? UiState.Success)?.data?.totalCost ?: "—"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "EC2 INSTANCES",
                    value = ec2Count.toString(),
                    sub = "$ec2Running running",
                    accentColor = Green,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "S3 BUCKETS",
                    value = s3Count.toString(),
                    accentColor = Blue,
                    modifier = Modifier.weight(1f)
                )
            }

            // Cost section
            SectionHeader("Cost Overview (MTD)")
            when (val s = costState) {
                is UiState.Loading -> ShimmerItem(modifier = Modifier.fillMaxWidth().height(140.dp))
                is UiState.Success -> {
                    val cost = s.data
                    if (s.isMock) MockBanner()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AwsDarkSurface, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Total MTD Cost", fontSize = 12.sp, color = TextTertiary)
                                Text(
                                    "$${cost.totalCost ?: "0.00"}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(cost.currency ?: "USD", fontSize = 13.sp, color = TextTertiary)
                        }
                        HorizontalDivider(color = BorderColor)
                        cost.services.take(5).forEach { svc ->
                            val amount = svc.amount.toDoubleOrNull() ?: 0.0
                            val total  = cost.totalCost?.toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0
                            val pct    = (amount / total).toFloat()
                            MetricBar(
                                label = svc.service.replace("Amazon ", "").replace("AWS ", "").take(28),
                                value = "$${"%.2f".format(amount)}",
                                progress = pct,
                                color = AwsOrange
                            )
                        }
                    }
                }
                is UiState.Error -> ErrorState(s.message, onRetry = { viewModel.loadCost() })
                else -> {}
            }
        }
    }
}
