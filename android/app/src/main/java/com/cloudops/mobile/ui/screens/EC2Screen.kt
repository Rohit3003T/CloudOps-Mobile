package com.cloudops.mobile.ui.screens

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
import com.cloudops.mobile.data.models.EC2Instance
import com.cloudops.mobile.ui.Screen
import com.cloudops.mobile.ui.components.*
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun EC2Screen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onViewMetrics: (String) -> Unit
) {
    val state by viewModel.instances.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchInstances() }

    Scaffold(
        topBar = {
            CloudOpsTopBar(
                title = "EC2 Instances",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.fetchInstances() }) {
                        Icon(Icons.Default.Refresh, null, tint = AwsLightText)
                    }
                }
            )
        },
        containerColor = AwsDark
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen()
            state.error != null -> Box(
                Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter
            ) {
                ErrorCard(state.error!!) { viewModel.fetchInstances() }
            }
            else -> {
                val instances = state.data?.instances ?: emptyList()
                if (instances.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Computer, null, tint = AwsGray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No EC2 instances found", color = AwsGray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SectionHeader("${instances.size} instance(s) found")
                        }
                        items(instances) { instance ->
                            EC2InstanceCard(instance, onViewMetrics = { onViewMetrics(instance.instanceId) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EC2InstanceCard(instance: EC2Instance, onViewMetrics: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AwsDarkCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Computer, null, tint = AwsOrange, modifier = Modifier.size(20.dp))
                    Text(instance.name, fontWeight = FontWeight.Bold, color = AwsLightText, fontSize = 15.sp)
                }
                StatusBadge(instance.state)
            }

            HorizontalDivider(color = AwsDark, thickness = 1.dp)

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                DetailItem("Instance ID", instance.instanceId, Modifier.weight(1f))
                DetailItem("Type", instance.instanceType, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                DetailItem("Public IP", instance.publicIp ?: "None", Modifier.weight(1f))
                DetailItem("Private IP", instance.privateIp ?: "None", Modifier.weight(1f))
            }
            DetailItem("Availability Zone", instance.az ?: "Unknown")

            if (instance.state == "running") {
                TextButton(
                    onClick = onViewMetrics,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BarChart, null, tint = AwsOrange, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View CloudWatch Metrics", color = AwsOrange, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, fontSize = 11.sp, color = AwsGray)
        Text(value, fontSize = 13.sp, color = AwsLightText, fontWeight = FontWeight.Medium)
    }
}
