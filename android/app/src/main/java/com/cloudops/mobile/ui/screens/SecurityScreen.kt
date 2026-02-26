package com.cloudops.mobile.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudops.mobile.data.models.SecurityFinding
import com.cloudops.mobile.ui.components.*
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun SecurityScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val state by viewModel.securityPosture.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchSecurityPosture() }

    Scaffold(
        topBar = {
            CloudOpsTopBar("Security Posture", onBack = onBack, actions = {
                IconButton(onClick = { viewModel.fetchSecurityPosture() }) {
                    Icon(Icons.Default.Refresh, null, tint = AwsLightText)
                }
            })
        },
        containerColor = AwsDark
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen()
            state.error != null -> Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
                ErrorCard(state.error!!) { viewModel.fetchSecurityPosture() }
            }
            state.data != null -> {
                val posture = state.data!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Score card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Security Score", fontSize = 16.sp, color = AwsGray)
                                ScoreGauge(score = posture.score)
                                Text(
                                    posture.posture,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor(posture.score)
                                )
                            }
                        }
                    }

                    // Severity summary
                    item {
                        SectionHeader("Findings Summary")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SeverityCount("CRITICAL", posture.critical, AwsRed, Modifier.weight(1f))
                            SeverityCount("HIGH", posture.high, Color(0xFFFF5722), Modifier.weight(1f))
                            SeverityCount("MEDIUM", posture.medium, AwsYellow, Modifier.weight(1f))
                            SeverityCount("LOW", posture.low, AwsGray, Modifier.weight(1f))
                        }
                    }

                    // Findings list
                    if (posture.findings.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = AwsGreen.copy(0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = AwsGreen, modifier = Modifier.size(24.dp))
                                    Column {
                                        Text("No issues found!", fontWeight = FontWeight.Bold, color = AwsGreen)
                                        Text("Your AWS account looks secure.", color = AwsGray, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        item { SectionHeader("${posture.findings.size} Finding(s)") }
                        items(posture.findings) { finding -> FindingCard(finding) }
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun ScoreGauge(score: Int) {
    val color = scoreColor(score)
    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            val padding = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val startAngle = 135f
            val sweepAngle = 270f

            // Background arc
            drawArc(
                color = AwsDark,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            // Score arc
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle * (score / 100f),
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            "$score",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

fun scoreColor(score: Int) = when {
    score >= 80 -> AwsGreen
    score >= 60 -> AwsYellow
    score >= 40 -> Color(0xFFFF5722)
    else -> AwsRed
}

@Composable
fun SeverityCount(label: String, count: Int, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.1f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$count", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 10.sp, color = color.copy(0.8f), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FindingCard(finding: SecurityFinding) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AwsDarkCard)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = AwsYellow, modifier = Modifier.size(18.dp))
                    Text(finding.resource, fontWeight = FontWeight.SemiBold, color = AwsLightText, fontSize = 13.sp, maxLines = 1)
                }
                SeverityBadge(finding.severity)
            }
            Text(finding.message, color = AwsGray, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}
