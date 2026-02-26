package com.cloudops.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudops.mobile.data.models.Budget
import com.cloudops.mobile.data.models.ServiceCost
import com.cloudops.mobile.ui.components.*
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun CostScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val costState by viewModel.currentCost.collectAsState()
    val trendState by viewModel.costTrend.collectAsState()
    val budgetsState by viewModel.budgets.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCurrentCost()
        viewModel.fetchCostTrend()
        viewModel.fetchBudgets()
    }

    Scaffold(
        topBar = {
            CloudOpsTopBar("Cost & Budget", onBack = onBack, actions = {
                IconButton(onClick = {
                    viewModel.fetchCurrentCost()
                    viewModel.fetchCostTrend()
                    viewModel.fetchBudgets()
                }) {
                    Icon(Icons.Default.Refresh, null, tint = AwsLightText)
                }
            })
        },
        containerColor = AwsDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current month total
            item {
                SectionHeader("Current Month")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
                ) {
                    when {
                        costState.isLoading -> Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AwsOrange, modifier = Modifier.size(32.dp))
                        }
                        costState.error != null -> Box(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(costState.error!!, color = AwsRed)
                        }
                        else -> {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("$", fontSize = 20.sp, color = AwsYellow, fontWeight = FontWeight.Bold)
                                    Text(
                                        costState.data?.totalCost ?: "0.00",
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AwsLightText
                                    )
                                    Text(costState.data?.currency ?: "USD", fontSize = 14.sp, color = AwsGray, modifier = Modifier.padding(bottom = 6.dp))
                                }
                                val period = costState.data?.period
                                if (period != null) {
                                    Text(
                                        "${period["start"]} → ${period["end"]}",
                                        fontSize = 12.sp, color = AwsGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top services
            if (costState.data != null) {
                val topServices = costState.data!!.byService.take(8)
                item { SectionHeader("Top Services by Cost") }
                items(topServices) { service -> ServiceCostRow(service) }
            }

            // Cost trend
            if (trendState.data != null) {
                val trend = trendState.data!!.trend
                item { SectionHeader("6-Month Cost Trend") }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val maxCost = trend.maxOfOrNull { it.cost.toDoubleOrNull() ?: 0.0 }?.coerceAtLeast(0.01) ?: 1.0
                            trend.forEach { month ->
                                val cost = month.cost.toDoubleOrNull() ?: 0.0
                                val fraction = (cost / maxCost).toFloat().coerceIn(0f, 1f)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(month.period.substring(0, 7), color = AwsGray, fontSize = 12.sp)
                                        Text("$${month.cost}", color = AwsLightText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = AwsYellow,
                                        trackColor = AwsDark,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Budgets
            item { SectionHeader("AWS Budgets") }
            when {
                budgetsState.isLoading -> item {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AwsOrange, modifier = Modifier.size(28.dp))
                    }
                }
                budgetsState.error != null -> item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AwsGray.copy(0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, null, tint = AwsGray, modifier = Modifier.size(18.dp))
                            Text("Budgets unavailable: ${budgetsState.error}", color = AwsGray, fontSize = 13.sp)
                        }
                    }
                }
                budgetsState.data?.budgets?.isEmpty() == true -> item {
                    Text("No budgets configured in AWS", color = AwsGray, fontSize = 13.sp)
                }
                else -> items(budgetsState.data?.budgets ?: emptyList()) { budget ->
                    BudgetCard(budget)
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun ServiceCostRow(service: ServiceCost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Cloud, null, tint = AwsOrange.copy(0.7f), modifier = Modifier.size(16.dp))
                Text(service.service, color = AwsLightText, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
            }
            Text("$${service.cost}", color = AwsYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
fun BudgetCard(budget: Budget) {
    val actual = budget.actual?.amount?.toDoubleOrNull() ?: 0.0
    val limit = budget.limit.amount?.toDoubleOrNull() ?: 1.0
    val fraction = (actual / limit).toFloat().coerceIn(0f, 1f)
    val isOver = actual > limit
    val color = when {
        fraction > 1f -> AwsRed
        fraction > 0.8f -> AwsYellow
        else -> AwsGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(budget.name, fontWeight = FontWeight.Bold, color = AwsLightText)
                if (isOver) {
                    Box(
                        modifier = Modifier.background(AwsRed.copy(0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text("EXCEEDED", color = AwsRed, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spent: $${String.format("%.2f", actual)}", color = color, fontSize = 13.sp)
                Text("Limit: $${budget.limit.amount ?: "--"}", color = AwsGray, fontSize = 13.sp)
            }
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = AwsDark
            )
            budget.forecast?.amount?.let { forecast ->
                Text("Forecast: $$forecast ${budget.limit.unit ?: ""}", color = AwsGray, fontSize = 12.sp)
            }
        }
    }
}
