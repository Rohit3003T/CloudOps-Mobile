package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.BorderStroke
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
import com.cloudmonitor.app.data.model.EbsVolume
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.util.formatDate
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

@Composable
fun EBSScreen(viewModel: CloudMonitorViewModel) {
    val ebsState by viewModel.ebs.collectAsState()

    LaunchedEffect(Unit) {
        if (ebsState is UiState.Idle) viewModel.loadEbs()
    }

    ScreenScaffold(
        title = "EBS Volumes",
        actions = {
            IconButton(onClick = { viewModel.loadEbs() }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        when (val s = ebsState) {
            is UiState.Loading -> LoadingState("Fetching EBS volumes…")
            is UiState.Error   -> ErrorState(s.message, onRetry = { viewModel.loadEbs() })
            is UiState.Success -> {
                val data = s.data
                val totalGib = data.volumes.sumOf { it.size ?: 0 }
                val inUse    = data.volumes.count { it.state.lowercase().contains("use") }
                val avail    = data.volumes.count { it.state.lowercase() == "available" }

                Column {
                    if (s.isMock) {
                        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { MockBanner() }
                    }
                    // Summary row
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip("${data.count} volumes")
                        InfoChip("${totalGib} GiB total")
                        if (inUse > 0) InfoChip("$inUse in-use", Blue)
                        if (avail > 0) InfoChip("$avail available", Green)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(data.volumes) { vol -> EBSVolumeCard(vol) }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun EBSVolumeCard(vol: EbsVolume) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DataUsage, null, tint = Purple, modifier = Modifier.size(18.dp))
                    Text(
                        text = vol.tags?.get("Name") ?: vol.volumeId,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
                StatusBadge(vol.state)
            }

            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

            // ID + size row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("VOLUME ID", fontSize = 9.sp, color = TextTertiary,
                        letterSpacing = 0.08.sp, fontWeight = FontWeight.Bold)
                    Text(vol.volumeId, fontSize = 11.sp, color = TextSecondary,
                        fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SIZE / TYPE", fontSize = 9.sp, color = TextTertiary,
                        letterSpacing = 0.08.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${vol.size ?: "?"} GiB · ${vol.volumeType ?: "?"}",
                        fontSize = 11.sp, color = TextSecondary, fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Chips row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                vol.availabilityZone?.let { InfoChip(it) }
                vol.iops?.let { InfoChip("$it IOPS") }
                if (vol.encrypted == true) {
                    InfoChip("🔒 Encrypted", Green)
                }
            }

            // Attachment info
            val firstAttachment = vol.attachments?.firstOrNull()
            if (firstAttachment != null) {
                HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Computer, null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                    Text(
                        text = buildString {
                            append(firstAttachment.instanceId ?: "unknown")
                            firstAttachment.device?.let { append(" · $it") }
                            firstAttachment.state?.let { append(" (${it})") }
                        },
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }

            // Created date
            vol.createTime?.let {
                Text(
                    text = "Created ${it.formatDate()}",
                    fontSize = 10.sp,
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
