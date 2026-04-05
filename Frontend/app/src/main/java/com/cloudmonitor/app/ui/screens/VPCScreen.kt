package com.cloudmonitor.app.ui.screens

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
import com.cloudmonitor.app.data.model.*
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset

@Composable
fun VPCScreen(viewModel: CloudMonitorViewModel) {
    val vpcState    by viewModel.vpcs.collectAsState()
    val subnetState by viewModel.subnets.collectAsState()
    val sgState     by viewModel.sgs.collectAsState()
    var tab         by remember { mutableIntStateOf(0) }
    val tabs = listOf("VPCs", "Subnets", "Security Groups")

    LaunchedEffect(Unit) {
        if (vpcState is UiState.Idle) viewModel.loadVpc()
    }

    ScreenScaffold(
        title = "VPC",
        actions = {
            IconButton(onClick = { viewModel.loadVpc() }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        Column {
            TabRow(
                selectedTabIndex = tab,
                containerColor = AwsDarkSurface,
                contentColor = AwsOrange,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        color = AwsOrange
                    )
                }
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = tab == i,
                        onClick = { tab = i },
                        text = {
                            Text(title, fontSize = 13.sp,
                                color = if (tab == i) AwsOrange else TextTertiary)
                        }
                    )
                }
            }

            when (tab) {
                0 -> VpcTab(vpcState)
                1 -> SubnetTab(subnetState)
                2 -> SecurityGroupTab(sgState)
            }
        }
    }
}

@Composable
fun VpcTab(state: UiState<VpcResponse>) {
    when (state) {
        is UiState.Loading -> LoadingState("Fetching VPCs…")
        is UiState.Error   -> ErrorState(state.message)
        is UiState.Success -> {
            if (state.isMock) Box(Modifier.padding(16.dp)) { MockBanner() }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.data.vpcs) { vpc ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Hub, null, tint = Blue, modifier = Modifier.size(18.dp))
                                    Text(vpc.name ?: vpc.vpcId, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                }
                                vpc.state?.let { StatusBadge(it) }
                            }
                            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoChip(vpc.vpcId)
                                vpc.cidrBlock?.let { InfoChip(it) }
                                if (vpc.isDefault == true) InfoChip("Default", Blue)
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun SubnetTab(state: UiState<SubnetResponse>) {
    when (state) {
        is UiState.Loading -> LoadingState("Fetching subnets…")
        is UiState.Error   -> ErrorState(state.message)
        is UiState.Success -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.data.subnets) { subnet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(subnet.name ?: subnet.subnetId, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoChip(subnet.cidrBlock ?: "—")
                                subnet.availabilityZone?.let { InfoChip(it) }
                                if (subnet.mapPublicIpOnLaunch == true) InfoChip("Public", Green)
                            }
                            Text(
                                "${subnet.availableIpAddressCount ?: "?"} free IPs",
                                fontSize = 11.sp, color = TextTertiary, fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun SecurityGroupTab(state: UiState<SecurityGroupResponse>) {
    when (state) {
        is UiState.Loading -> LoadingState("Fetching security groups…")
        is UiState.Error   -> ErrorState(state.message)
        is UiState.Success -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.data.securityGroups) { sg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sg.groupName ?: sg.groupId, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    InfoChip("↓ ${sg.inboundRules?.size ?: 0}", Green)
                                    InfoChip("↑ ${sg.outboundRules?.size ?: 0}", Red)
                                }
                            }
                            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                            Text(sg.description ?: "—", fontSize = 12.sp, color = TextTertiary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoChip(sg.groupId)
                                sg.vpcId?.let { InfoChip(it) }
                            }
                            // Show first 2 inbound rules
                            sg.inboundRules?.take(2)?.forEach { rule ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${rule.protocol?.uppercase() ?: "ALL"} ${rule.fromPort ?: "All"}${if (rule.toPort != null && rule.toPort != rule.fromPort) "–${rule.toPort}" else ""}",
                                        fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        rule.ipRanges?.firstOrNull() ?: "—",
                                        fontSize = 11.sp, color = Blue, fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {}
    }
}
