package com.cloudops.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudops.mobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudOpsTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = AwsLightText)
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AwsLightText)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AwsDarkSurface
        )
    )
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = AwsOrange,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AwsDarkCard),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AwsLightText)
                Text(label, fontSize = 12.sp, color = AwsGray)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = AwsOrange,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AwsOrange)
    }
}

@Composable
fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AwsRed.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(message, color = AwsRed, fontSize = 14.sp)
            TextButton(onClick = onRetry) {
                Text("Retry", color = AwsOrange)
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, bgColor) = when (status.lowercase()) {
        "running" -> AwsGreen to AwsGreen.copy(alpha = 0.15f)
        "stopped" -> AwsGray to AwsGray.copy(alpha = 0.15f)
        "pending" -> AwsYellow to AwsYellow.copy(alpha = 0.15f)
        "terminated" -> AwsRed to AwsRed.copy(alpha = 0.15f)
        else -> AwsGray to AwsGray.copy(alpha = 0.15f)
    }
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status.uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SeverityBadge(severity: String) {
    val color = when (severity) {
        "CRITICAL" -> AwsRed
        "HIGH" -> Color(0xFFFF5722)
        "MEDIUM" -> AwsYellow
        "LOW" -> AwsGray
        else -> AwsGray
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(severity, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
