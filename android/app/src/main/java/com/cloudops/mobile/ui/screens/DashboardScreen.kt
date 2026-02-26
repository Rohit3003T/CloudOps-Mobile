package com.cloudops.mobile.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudops.mobile.ui.Screen
import com.cloudops.mobile.ui.components.*
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

data class DashboardMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconTint: androidx.compose.ui.graphics.Color,
    val route: String
)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val awsStatus by viewModel.awsStatus.collectAsState()
    val ec2Summary by viewModel.ec2Summary.collectAsState()
    val currentCost by viewModel.currentCost.collectAsState()
    val userName by viewModel.userName.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAwsStatus()
    }

    LaunchedEffect(awsStatus.data?.connected) {
        if (awsStatus.data?.connected == true) {
            viewModel.fetchEC2Summary()
            viewModel.fetchCurrentCost()
        }
    }

    Scaffold(
        topBar = {
            CloudOpsTopBar(
                title = "CloudOps Mobile",
                actions = {
                    IconButton(onClick = { onNavigate(Screen.Profile.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = AwsLightText)
                    }
                }
            )
        },
        containerColor = AwsDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AwsOrange.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, AwsOrange.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.WavingHand, null, tint = AwsOrange, modifier = Modifier.size(28.dp))
                    Column {
                        Text(
                            "Welcome back, ${userName ?: "User"}!",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AwsLightText
                        )
                        Text(
                            if (awsStatus.data?.connected == true)
                                "Connected to AWS account ${awsStatus.data?.accountId}"
                            else "No AWS account connected",
                            fontSize = 12.sp, color = AwsGray
                        )
                    }
                }
            }

            // AWS Connection status
            if (awsStatus.data?.connected != true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.ConnectAws.route) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AwsBlue.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, AwsBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.LinkOff, null, tint = AwsBlue, modifier = Modifier.size(24.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Connect AWS Account", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AwsLightText)
                            Text("Tap to connect your AWS credentials", fontSize = 12.sp, color = AwsGray)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = AwsGray)
                    }
                }
            }

            // Quick stats
            if (awsStatus.data?.connected == true) {
                SectionHeader("Quick Overview")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Running",
                        value = ec2Summary.data?.running?.toString() ?: "--",
                        icon = Icons.Default.PlayArrow,
                        iconTint = AwsGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Stopped",
                        value = ec2Summary.data?.stopped?.toString() ?: "--",
                        icon = Icons.Default.Stop,
                        iconTint = AwsGray,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        label = "Total EC2",
                        value = ec2Summary.data?.total?.toString() ?: "--",
                        icon = Icons.Default.Computer,
                        iconTint = AwsOrange,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Month Cost",
                        value = if (currentCost.data != null) "$${currentCost.data!!.totalCost}" else "--",
                        icon = Icons.Default.AttachMoney,
                        iconTint = AwsYellow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SectionHeader("Services")

            val menuItems = listOf(
                DashboardMenuItem("EC2 Instances", "Monitor compute instances", Icons.Default.Computer, AwsOrange, Screen.EC2.route),
                DashboardMenuItem("S3 Buckets", "View storage buckets", Icons.Default.Storage, AwsBlue, Screen.S3.route),
                DashboardMenuItem("Cost & Budget", "Track AWS spending", Icons.Default.AttachMoney, AwsYellow, Screen.Cost.route),
                DashboardMenuItem("Security", "Security posture insights", Icons.Default.Security, AwsRed, Screen.Security.route),
                DashboardMenuItem("AWS Account", "Manage AWS connection", Icons.Default.Link, AwsGreen, Screen.ConnectAws.route),
            )

            menuItems.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(item.route) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AwsDarkCard),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(item.iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, null, tint = item.iconTint, modifier = Modifier.size(24.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(item.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AwsLightText)
                            Text(item.subtitle, fontSize = 12.sp, color = AwsGray)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = AwsGray)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
