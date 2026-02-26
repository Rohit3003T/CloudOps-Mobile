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
import com.cloudops.mobile.data.models.S3Bucket
import com.cloudops.mobile.ui.components.*
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun S3Screen(viewModel: MainViewModel, onBack: () -> Unit) {
    val state by viewModel.buckets.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchBuckets() }

    Scaffold(
        topBar = {
            CloudOpsTopBar(
                title = "S3 Buckets",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.fetchBuckets() }) {
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
                ErrorCard(state.error!!) { viewModel.fetchBuckets() }
            }
            else -> {
                val buckets = state.data?.buckets ?: emptyList()
                val publicBuckets = buckets.count { it.isPublic }

                if (buckets.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Storage, null, tint = AwsGray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No S3 buckets found", color = AwsGray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    label = "Total",
                                    value = buckets.size.toString(),
                                    icon = Icons.Default.Storage,
                                    iconTint = AwsBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Public",
                                    value = publicBuckets.toString(),
                                    icon = Icons.Default.Public,
                                    iconTint = if (publicBuckets > 0) AwsRed else AwsGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        item { SectionHeader("${buckets.size} bucket(s)") }
                        items(buckets) { bucket -> S3BucketCard(bucket) }
                    }
                }
            }
        }
    }
}

@Composable
fun S3BucketCard(bucket: S3Bucket) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AwsDarkCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Storage, null, tint = AwsBlue, modifier = Modifier.size(20.dp))
                    Text(
                        bucket.name,
                        fontWeight = FontWeight.Bold,
                        color = AwsLightText,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
                if (bucket.isPublic) {
                    Box(
                        modifier = Modifier
                            .background(AwsRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("PUBLIC", color = AwsRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(AwsGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("PRIVATE", color = AwsGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = AwsDark, thickness = 1.dp)

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                DetailItem("Region", bucket.region, Modifier.weight(1f))
                DetailItem(
                    "Created",
                    bucket.createdAt?.substring(0, 10) ?: "Unknown",
                    Modifier.weight(1f)
                )
            }

            if (bucket.isPublic) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = AwsRed, modifier = Modifier.size(14.dp))
                    Text(
                        "Public access may expose your data",
                        color = AwsRed, fontSize = 12.sp
                    )
                }
            }
        }
    }
}
