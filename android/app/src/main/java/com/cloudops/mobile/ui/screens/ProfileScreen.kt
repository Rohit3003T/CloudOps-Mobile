package com.cloudops.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.cloudops.mobile.ui.components.CloudOpsTopBar
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun ProfileScreen(viewModel: MainViewModel, onBack: () -> Unit, onLogout: () -> Unit) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val awsStatus by viewModel.awsStatus.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = AwsDarkCard,
            title = { Text("Sign Out", color = AwsLightText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out of CloudOps Mobile?", color = AwsGray) },
            confirmButton = {
                Button(
                    onClick = { viewModel.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = AwsRed)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = AwsGray)
                }
            }
        )
    }

    Scaffold(
        topBar = { CloudOpsTopBar("Profile", onBack = onBack) },
        containerColor = AwsDark
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(AwsOrange.copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            (userName?.firstOrNull()?.uppercaseChar() ?: "U").toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AwsOrange
                        )
                    }
                    Text(userName ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AwsLightText)
                    Text(userEmail ?: "", fontSize = 14.sp, color = AwsGray)
                }
            }

            // AWS info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("AWS Account", fontWeight = FontWeight.SemiBold, color = AwsOrange, fontSize = 14.sp)

                    val status = awsStatus.data
                    if (status?.connected == true) {
                        ProfileRow(Icons.Default.CheckCircle, "Account ID", status.accountId ?: "--", AwsGreen)
                        ProfileRow(Icons.Default.Public, "Region", status.region ?: "--", AwsBlue)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LinkOff, null, tint = AwsGray, modifier = Modifier.size(18.dp))
                            Text("No AWS account connected", color = AwsGray, fontSize = 13.sp)
                        }
                    }
                }
            }

            // About
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("About", fontWeight = FontWeight.SemiBold, color = AwsOrange, fontSize = 14.sp)
                    ProfileRow(Icons.Default.Apps, "App", "CloudOps Mobile", AwsGray)
                    ProfileRow(Icons.Default.Code, "Version", "1.0.0", AwsGray)
                    ProfileRow(Icons.Default.Build, "Stack", "Kotlin + Jetpack Compose", AwsGray)
                    ProfileRow(Icons.Default.Cloud, "Backend", "Node.js + Express + AWS SDK", AwsGray)
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AwsRed.copy(0.15f)),
            ) {
                Icon(Icons.Default.Logout, null, tint = AwsRed)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out", color = AwsRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, tint: androidx.compose.ui.graphics.Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Column {
            Text(label, fontSize = 11.sp, color = AwsGray)
            Text(value, fontSize = 13.sp, color = AwsLightText)
        }
    }
}
