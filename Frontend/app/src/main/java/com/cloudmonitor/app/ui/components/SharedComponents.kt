package com.cloudmonitor.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmonitor.app.ui.theme.*

// ── Loading spinner ────────────────────────────────────────────────────────────

@Composable
fun LoadingState(message: String = "Loading…") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = AwsOrange, strokeWidth = 3.dp)
            Text(message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

// ── Error state ────────────────────────────────────────────────────────────────

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Red,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (onRetry != null) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = AwsOrange, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Retry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Skeleton shimmer ───────────────────────────────────────────────────────────

@Composable
fun ShimmerItem(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(AwsDarkContainer, AwsDarkVariant, AwsDarkContainer),
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(shimmerBrush)
    )
}

// ── Stat card ──────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    label: String,
    value: String,
    sub: String? = null,
    accentColor: Color = AwsOrange,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor)
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                color = TextPrimary, fontFamily = FontFamily.Monospace)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                color = TextTertiary, letterSpacing = 0.06.sp,
                modifier = Modifier.padding(top = 4.dp))
            if (sub != null) {
                Text(sub, fontSize = 11.sp, color = TextTertiary,
                    fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// ── Status badge ───────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "running", "available", "active", "Active" -> Pair(Color(0x1F22D87A), Green)
        "stopped", "terminated", "error" -> Pair(Color(0x1FFF4757), Red)
        "pending", "stopping", "in-use", "in_use" -> Pair(Color(0x1FFFD166), Yellow)
        "completed", "success" -> Pair(Color(0x1F22D87A), Green)
        "failure", "timed_out" -> Pair(Color(0x1FFF4757), Red)
        "in_progress", "queued" -> Pair(Color(0x1FFFD166), Yellow)
        else -> Pair(Color(0x1F576F87), TextSecondary)
    }

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(fg, CircleShape)
        )
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── Info chip / badge ──────────────────────────────────────────────────────────

@Composable
fun InfoChip(text: String, color: Color = TextSecondary) {
    Text(
        text = text,
        modifier = Modifier
            .background(AwsDarkContainer, RoundedCornerShape(4.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        fontFamily = FontFamily.Monospace
    )
}

// ── Mock data banner ───────────────────────────────────────────────────────────

@Composable
fun MockBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1FFFD166), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0x33FFD166), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("◈", color = Yellow, fontSize = 14.sp)
        Text(
            "Demo mode — showing mock data. Configure AWS credentials in backend .env",
            color = Yellow, fontSize = 12.sp
        )
    }
}

// ── Section header ─────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.08.sp,
        color = TextTertiary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

// ── Detail row (key / value) ───────────────────────────────────────────────────

@Composable
fun DetailRow(
    label: String,
    value: String?,
    mono: Boolean = false,
    valueColor: Color = TextSecondary
) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextTertiary, fontSize = 12.sp, modifier = Modifier.weight(0.4f))
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.Default,
            modifier = Modifier.weight(0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 2
        )
    }
    HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
}

// ── Simple metric bar ──────────────────────────────────────────────────────────

@Composable
fun MetricBar(
    label: String,
    value: String,
    progress: Float,
    color: Color = AwsOrange
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(value, color = color, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = AwsDarkContainer
        )
    }
}

// ── Screen scaffold wrapper ────────────────────────────────────────────────────

@Composable
fun ScreenScaffold(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AwsDark)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AwsDarkSurface)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { actions() }
        }
        HorizontalDivider(color = BorderColor)
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
