package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmonitor.app.data.model.Ec2Instance
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

@Composable
fun EC2Screen(viewModel: CloudMonitorViewModel) {
    val ec2State by viewModel.ec2.collectAsState()
    var selected by remember { mutableStateOf<Ec2Instance?>(null) }

    LaunchedEffect(Unit) {
        if (ec2State is UiState.Idle) viewModel.loadEc2()
    }

    ScreenScaffold(
        title = "EC2 Instances",
        actions = {
            IconButton(onClick = { viewModel.loadEc2() }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        when (val s = ec2State) {
            is UiState.Loading -> LoadingState("Fetching EC2 instances…")
            is UiState.Error   -> ErrorState(s.message, onRetry = { viewModel.loadEc2() })
            is UiState.Success -> {
                val data = s.data
                Column {
                    if (s.isMock) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { MockBanner() }
                    }
                    // Summary chips
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip("${data.count} total")
                        InfoChip("${data.instances.count { it.state == "running" }} running", Green)
                        InfoChip("${data.instances.count { it.state == "stopped" }} stopped", Red)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(data.instances) { instance ->
                            EC2InstanceCard(instance = instance, onClick = { selected = instance })
                        }
                    }
                }
            }
            else -> {}
        }
    }

    selected?.let { instance ->
        EC2DetailSheet(instance = instance, onDismiss = { selected = null })
    }
}

@Composable
fun EC2InstanceCard(instance: Ec2Instance, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Computer, null, tint = Green, modifier = Modifier.size(18.dp))
                    Text(
                        instance.name ?: instance.instanceId,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                }
                StatusBadge(instance.state)
            }
            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(instance.instanceType)
                instance.availabilityZone?.let { InfoChip(it) }
                instance.platform?.let { InfoChip(it) }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Instance ID", fontSize = 10.sp, color = TextTertiary)
                    Text(instance.instanceId, fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Public IP", fontSize = 10.sp, color = TextTertiary)
                    Text(instance.publicIp ?: "—", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EC2DetailSheet(instance: Ec2Instance, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AwsDarkSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Computer, null, tint = Green, modifier = Modifier.size(22.dp))
                Column {
                    Text(instance.name ?: "Unnamed", style = MaterialTheme.typography.titleMedium)
                    StatusBadge(instance.state)
                }
            }
            HorizontalDivider(color = BorderColor)
            DetailRow("Instance ID",    instance.instanceId, mono = true)
            DetailRow("Instance Type",  instance.instanceType, mono = true)
            DetailRow("Image ID",       instance.imageId, mono = true)
            DetailRow("Public IP",      instance.publicIp, mono = true)
            DetailRow("Private IP",     instance.privateIp, mono = true)
            DetailRow("Public DNS",     instance.publicDns)
            DetailRow("AZ",             instance.availabilityZone, mono = true)
            DetailRow("VPC ID",         instance.vpcId, mono = true)
            DetailRow("Subnet ID",      instance.subnetId, mono = true)
            DetailRow("Key Pair",       instance.keyName, mono = true)
            DetailRow("Platform",       instance.platform)
            DetailRow("Architecture",   instance.architecture)
            instance.launchTime?.let {
                DetailRow("Launched", it.take(19).replace("T", " "))
            }
            instance.securityGroups?.let { sgs ->
                if (sgs.isNotEmpty()) {
                    DetailRow("Security Groups", sgs.joinToString(", ") { it.name ?: it.id })
                }
            }
        }
    }
}
