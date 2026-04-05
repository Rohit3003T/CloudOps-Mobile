package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmonitor.app.data.model.WorkflowRun
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.util.formatDuration
import com.cloudmonitor.app.util.shortSha
import com.cloudmonitor.app.util.truncate
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

private fun conclusionColor(conclusion: String?, status: String?): Color {
    if (status == "in_progress" || status == "queued") return Yellow
    return when (conclusion?.lowercase()) {
        "success"              -> Green
        "failure", "timed_out" -> Red
        "cancelled"            -> TextSecondary
        else                   -> TextSecondary
    }
}

private fun conclusionIcon(conclusion: String?, status: String?): String {
    if (status == "in_progress") return "⟳"
    if (status == "queued")      return "◎"
    return when (conclusion?.lowercase()) {
        "success"   -> "✓"
        "failure"   -> "✕"
        "cancelled" -> "⊘"
        else        -> "○"
    }
}

@Composable
fun CICDScreen(viewModel: CloudMonitorViewModel) {
    val cicdState by viewModel.cicd.collectAsState()
    val owner     by viewModel.cicdOwner.collectAsState()
    val repo      by viewModel.cicdRepo.collectAsState()
    val focus     = LocalFocusManager.current
    var selected  by remember { mutableStateOf<WorkflowRun?>(null) }

    val doLoad = {
        focus.clearFocus()
        if (owner.isNotBlank() && repo.isNotBlank()) {
            viewModel.loadCicd(owner.trim(), repo.trim())
        }
    }

    ScreenScaffold(
        title = "GitHub Actions",
        actions = {
            if (owner.isNotBlank() && repo.isNotBlank()) {
                IconButton(onClick = doLoad) {
                    Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Config panel ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AwsDarkSurface)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = owner,
                        onValueChange = { viewModel.setCicdOwner(it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Owner / Org") },
                        placeholder = { Text("myorg", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = cicdFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = repo,
                        onValueChange = { viewModel.setCicdRepo(it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Repository") },
                        placeholder = { Text("myapp", fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { doLoad() }),
                        colors = cicdFieldColors(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Button(
                    onClick = doLoad,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    enabled = owner.isNotBlank() && repo.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AwsOrange,
                        contentColor = AwsDark,
                        disabledContainerColor = Color(0xFF5C4200)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Loop, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Load Workflow Runs", fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = BorderColor)

            // ── Content ───────────────────────────────────────────────────────
            when (val s = cicdState) {
                is UiState.Loading -> LoadingState("Fetching workflow runs…")

                is UiState.Error -> {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ErrorState(s.message, onRetry = doLoad)
                        if (s.message.contains("501") || s.message.contains("token", ignoreCase = true)
                            || s.message.contains("GITHUB", ignoreCase = true)) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x1FFFD166), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("💡", fontSize = 14.sp)
                                Text(
                                    "Set GITHUB_TOKEN in your backend .env to enable CI/CD tracking.",
                                    fontSize = 12.sp, color = Yellow, lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val runs    = s.data.runs
                    val success = runs.count { it.conclusion == "success" }
                    val failed  = runs.count { it.conclusion == "failure" || it.conclusion == "timed_out" }
                    val running = runs.count { it.status == "in_progress" }

                    Column {
                        // Summary chips
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoChip("${s.data.totalCount ?: runs.size} runs")
                            if (success > 0) InfoChip("✓ $success", Green)
                            if (failed  > 0) InfoChip("✕ $failed",  Red)
                            if (running > 0) InfoChip("⟳ $running", Yellow)
                        }

                        if (runs.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No workflow runs found for $owner/$repo", color = TextSecondary, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(runs, key = { it.id }) { run ->
                                    WorkflowRunCard(run = run, onClick = { selected = run })
                                }
                            }
                        }
                    }
                }

                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("⟳", fontSize = 48.sp, color = TextTertiary)
                            Text("Enter a GitHub owner and repo above",
                                color = TextSecondary, fontSize = 14.sp)
                            Text("Requires GITHUB_TOKEN in backend .env",
                                color = TextTertiary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    selected?.let { run ->
        WorkflowRunSheet(run = run, onDismiss = { selected = null })
    }
}

@Composable
fun WorkflowRunCard(run: WorkflowRun, onClick: () -> Unit) {
    val color = conclusionColor(run.conclusion, run.status)
    val icon  = conclusionIcon(run.conclusion, run.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Status + run number + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(icon, color = color, fontSize = 16.sp, fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold)
                    Text(
                        "#${run.runNumber}",
                        fontWeight = FontWeight.Bold,
                        color = color,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    StatusBadge(run.conclusion ?: run.status ?: "unknown")
                }
                Text(
                    run.createdAt?.take(10) ?: "—",
                    fontSize = 11.sp, color = TextTertiary, fontFamily = FontFamily.Monospace
                )
            }

            // Workflow name
            Text(
                run.name ?: "Workflow Run",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 1
            )

            // Commit message
            run.commitMessage?.takeIf { it.isNotBlank() }?.let { msg ->
                Text(
                    msg.truncate(72),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = 17.sp
                )
            }

            // Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                run.branch?.let { InfoChip(it) }
                run.triggerEvent?.let { InfoChip(it) }
                run.actor?.let { InfoChip("@${it}") }
                run.durationMs?.let { InfoChip(it.formatDuration()) }
            }

            // Commit SHA
            run.commitSha?.let { sha ->
                Text(
                    sha.shortSha(),
                    fontSize = 10.sp, color = TextTertiary, fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowRunSheet(run: WorkflowRun, onDismiss: () -> Unit) {
    val color = conclusionColor(run.conclusion, run.status)
    val icon  = conclusionIcon(run.conclusion, run.status)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AwsDarkSurface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(icon, color = color, fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        run.name ?: "Run #${run.runNumber}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge(run.conclusion ?: run.status ?: "unknown")
                        run.branch?.let { InfoChip(it) }
                    }
                }
            }

            HorizontalDivider(color = BorderColor)

            // Commit message
            run.commitMessage?.takeIf { it.isNotBlank() }?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(msg, fontSize = 13.sp, color = TextSecondary, lineHeight = 19.sp)
                HorizontalDivider(color = BorderColor, modifier = Modifier.padding(top = 8.dp))
            }

            DetailRow("Run Number",  "#${run.runNumber} (attempt #${run.runAttempt ?: 1})")
            DetailRow("Status",      run.status)
            DetailRow("Conclusion",  run.conclusion ?: "—")
            DetailRow("Branch",      run.branch, mono = true)
            DetailRow("Commit",      run.commitSha.shortSha(), mono = true)
            DetailRow("Actor",       run.actor?.let { "@$it" })
            DetailRow("Trigger",     run.triggerEvent)
            DetailRow("Duration",    run.durationMs.formatDuration())
            DetailRow("Started",     run.createdAt?.take(19)?.replace("T", " "))
            DetailRow("Finished",    run.updatedAt?.take(19)?.replace("T", " "))

            run.url?.let { url ->
                Spacer(Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, null, tint = AwsOrange, modifier = Modifier.size(14.dp))
                    Text(
                        text = url.removePrefix("https://github.com/"),
                        fontSize = 12.sp,
                        color = AwsOrange,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun cicdFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = AwsOrange,
    unfocusedBorderColor    = BorderColor,
    focusedTextColor        = TextPrimary,
    unfocusedTextColor      = TextSecondary,
    cursorColor             = AwsOrange,
    focusedLabelColor       = AwsOrange,
    unfocusedLabelColor     = TextTertiary,
    focusedContainerColor   = AwsDarkContainer,
    unfocusedContainerColor = AwsDarkContainer
)
