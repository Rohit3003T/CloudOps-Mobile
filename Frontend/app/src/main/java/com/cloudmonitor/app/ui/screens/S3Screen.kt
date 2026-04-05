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
import com.cloudmonitor.app.data.model.S3Bucket
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.util.formatDate
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

@Composable
fun S3Screen(viewModel: CloudMonitorViewModel) {
    val s3State by viewModel.s3.collectAsState()

    LaunchedEffect(Unit) {
        if (s3State is UiState.Idle) viewModel.loadS3()
    }

    ScreenScaffold(
        title = "S3 Buckets",
        actions = {
            IconButton(onClick = { viewModel.loadS3() }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        when (val s = s3State) {
            is UiState.Loading -> LoadingState("Fetching S3 buckets…")
            is UiState.Error   -> ErrorState(s.message, onRetry = { viewModel.loadS3() })
            is UiState.Success -> {
                val data = s.data
                Column {
                    if (s.isMock) {
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { MockBanner() }
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip("${data.count} bucket${if (data.count != 1) "s" else ""}")
                        data.owner?.takeIf { it.isNotBlank() }?.let { InfoChip("Owner: $it") }
                    }
                    if (data.buckets.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⬡", fontSize = 40.sp, color = TextTertiary)
                                Text("No buckets found", color = TextSecondary, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(data.buckets) { bucket -> S3BucketCard(bucket) }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun S3BucketCard(bucket: S3Bucket) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Storage,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(30.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = bucket.name,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Text(
                    text = "Created ${bucket.creationDate.formatDate()}",
                    fontSize = 12.sp,
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
