package com.cloudops.mobile.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudops.mobile.ui.components.CloudOpsTopBar
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

val AWS_REGIONS = listOf(
    "us-east-1" to "US East (N. Virginia)",
    "us-east-2" to "US East (Ohio)",
    "us-west-1" to "US West (N. California)",
    "us-west-2" to "US West (Oregon)",
    "eu-west-1" to "EU (Ireland)",
    "eu-central-1" to "EU (Frankfurt)",
    "ap-south-1" to "Asia Pacific (Mumbai)",
    "ap-southeast-1" to "Asia Pacific (Singapore)",
    "ap-southeast-2" to "Asia Pacific (Sydney)",
    "ap-northeast-1" to "Asia Pacific (Tokyo)",
    "sa-east-1" to "South America (São Paulo)",
)

@Composable
fun ConnectAwsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val awsStatus by viewModel.awsStatus.collectAsState()

    var accessKeyId by remember { mutableStateOf("") }
    var secretAccessKey by remember { mutableStateOf("") }
    var secretVisible by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf("us-east-1") }
    var regionExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<Pair<Boolean, String>?>(null) } // success, text

    val isConnected = awsStatus.data?.connected == true

    LaunchedEffect(Unit) { viewModel.fetchAwsStatus() }

    Scaffold(
        topBar = { CloudOpsTopBar("AWS Connection", onBack = onBack) },
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
            // Current status
            if (isConnected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AwsGreen.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, AwsGreen.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = AwsGreen, modifier = Modifier.size(20.dp))
                            Text("Connected to AWS", fontWeight = FontWeight.Bold, color = AwsGreen)
                        }
                        Text("Account ID: ${awsStatus.data?.accountId}", fontSize = 13.sp, color = AwsGray)
                        Text("Region: ${awsStatus.data?.region}", fontSize = 13.sp, color = AwsGray)
                        Text("ARN: ${awsStatus.data?.arn}", fontSize = 12.sp, color = AwsGray)

                        Spacer(Modifier.height(4.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.disconnectAws { message = Pair(false, "Disconnected from AWS") }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, AwsRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LinkOff, null, tint = AwsRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Disconnect AWS Account", color = AwsRed)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        if (isConnected) "Update AWS Credentials" else "Connect Your AWS Account",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AwsLightText
                    )

                    // Info box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AwsBlue.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AwsBlue.copy(alpha = 0.2f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, null, tint = AwsBlue, modifier = Modifier.size(18.dp))
                            Text(
                                "Use an IAM user with read-only permissions (ReadOnlyAccess policy). " +
                                "Credentials are stored in memory only during this session.",
                                fontSize = 12.sp, color = AwsGray, lineHeight = 18.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = accessKeyId,
                        onValueChange = { accessKeyId = it },
                        label = { Text("Access Key ID") },
                        leadingIcon = { Icon(Icons.Default.Key, null, tint = AwsGray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange, focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange, unfocusedBorderColor = AwsGray.copy(0.5f),
                            unfocusedLabelColor = AwsGray
                        )
                    )

                    OutlinedTextField(
                        value = secretAccessKey,
                        onValueChange = { secretAccessKey = it },
                        label = { Text("Secret Access Key") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = AwsGray) },
                        trailingIcon = {
                            IconButton(onClick = { secretVisible = !secretVisible }) {
                                Icon(if (secretVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = AwsGray)
                            }
                        },
                        visualTransformation = if (secretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange, focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange, unfocusedBorderColor = AwsGray.copy(0.5f),
                            unfocusedLabelColor = AwsGray
                        )
                    )

                    // Region dropdown
                    ExposedDropdownMenuBox(
                        expanded = regionExpanded,
                        onExpandedChange = { regionExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = AWS_REGIONS.find { it.first == selectedRegion }?.second ?: selectedRegion,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("AWS Region") },
                            leadingIcon = { Icon(Icons.Default.Public, null, tint = AwsGray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AwsOrange, focusedLabelColor = AwsOrange,
                                cursorColor = AwsOrange, unfocusedBorderColor = AwsGray.copy(0.5f),
                                unfocusedLabelColor = AwsGray
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = regionExpanded,
                            onDismissRequest = { regionExpanded = false }
                        ) {
                            AWS_REGIONS.forEach { (code, label) ->
                                DropdownMenuItem(
                                    text = { Text("$label ($code)", color = AwsLightText) },
                                    onClick = { selectedRegion = code; regionExpanded = false }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(message != null) {
                        val (isSuccess, text) = message ?: (false to "")
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuccess) AwsGreen.copy(0.1f) else AwsRed.copy(0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    null,
                                    tint = if (isSuccess) AwsGreen else AwsRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(text, color = if (isSuccess) AwsGreen else AwsRed, fontSize = 13.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (accessKeyId.isBlank() || secretAccessKey.isBlank()) {
                                message = Pair(false, "Please enter your AWS credentials")
                                return@Button
                            }
                            isLoading = true
                            message = null
                            viewModel.connectAws(
                                accessKeyId.trim(), secretAccessKey.trim(), selectedRegion,
                                onSuccess = { msg -> isLoading = false; message = Pair(true, msg) },
                                onError = { err -> isLoading = false; message = Pair(false, err) }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AwsOrange)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = AwsDark, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Link, null, tint = AwsDark)
                            Spacer(Modifier.width(8.dp))
                            Text("Verify & Connect", fontWeight = FontWeight.Bold, color = AwsDark)
                        }
                    }
                }
            }
        }
    }
}
