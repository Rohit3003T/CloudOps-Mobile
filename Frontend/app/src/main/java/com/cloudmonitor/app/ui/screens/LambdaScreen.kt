package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmonitor.app.data.model.LambdaFunction
import com.cloudmonitor.app.ui.components.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.util.formatBytes
import com.cloudmonitor.app.util.formatDate
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

private fun runtimeColor(runtime: String?): Color = when {
    runtime == null                     -> TextSecondary
    runtime.contains("node")            -> Green
    runtime.contains("python")          -> Blue
    runtime.contains("java")            -> Yellow
    runtime.contains("go")              -> Purple
    runtime.contains("ruby")            -> Red
    runtime.contains("dotnet")          -> Blue
    else                                -> TextSecondary
}

@Composable
fun LambdaScreen(viewModel: CloudMonitorViewModel) {
    val lambdaState by viewModel.lambda.collectAsState()
    var selected    by remember { mutableStateOf<LambdaFunction?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (lambdaState is UiState.Idle) viewModel.loadLambda()
    }

    ScreenScaffold(
        title = "Lambda Functions",
        actions = {
            IconButton(onClick = { viewModel.loadLambda() }) {
                Icon(Icons.Default.Refresh, "Refresh", tint = AwsOrange, modifier = Modifier.size(20.dp))
            }
        }
    ) {
        when (val s = lambdaState) {
            is UiState.Loading -> LoadingState("Fetching Lambda functions…")
            is UiState.Error   -> ErrorState(s.message, onRetry = { viewModel.loadLambda() })
            is UiState.Success -> {
                val all = s.data.functions
                val filtered = if (searchQuery.isBlank()) all
                               else all.filter { it.functionName.contains(searchQuery, ignoreCase = true) }

                Column {
                    if (s.isMock) {
                        Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { MockBanner() }
                    }

                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = { Text("Search functions…", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange,
                            unfocusedBorderColor = BorderColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextSecondary,
                            cursorColor = AwsOrange,
                            focusedContainerColor = AwsDarkContainer,
                            unfocusedContainerColor = AwsDarkContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Summary chips
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoChip("${filtered.size} of ${all.size}")
                        val runtimes = all.mapNotNull { it.runtime }.distinct()
                        if (runtimes.size <= 3) runtimes.forEach { InfoChip(it, runtimeColor(it)) }
                        else InfoChip("${runtimes.size} runtimes")
                    }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                if (searchQuery.isBlank()) "No functions found" else "No results for \"$searchQuery\"",
                                color = TextSecondary, fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered) { fn ->
                                LambdaCard(fn = fn, onClick = { selected = fn })
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    selected?.let { fn ->
        LambdaDetailSheet(fn = fn, onDismiss = { selected = null })
    }
}

@Composable
fun LambdaCard(fn: LambdaFunction, onClick: () -> Unit) {
    val color = runtimeColor(fn.runtime)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = AwsDarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("λ", color = Yellow, fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text(
                        fn.functionName,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }
                fn.runtime?.let { InfoChip(it, color) }
            }

            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)

            // Description
            if (!fn.description.isNullOrBlank()) {
                Text(
                    fn.description,
                    fontSize = 12.sp,
                    color = TextTertiary,
                    maxLines = 2,
                    lineHeight = 17.sp
                )
            }

            // Chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                fn.memorySize?.let { InfoChip("${it}MB") }
                fn.timeout?.let { InfoChip("${it}s") }
                fn.state?.let { StatusBadge(it) }
            }

            // Last modified
            fn.lastModified?.let {
                Text(
                    "Modified ${it.formatDate()}",
                    fontSize = 10.sp, color = TextTertiary, fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LambdaDetailSheet(fn: LambdaFunction, onDismiss: () -> Unit) {
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("λ", color = Yellow, fontSize = 26.sp,
                    fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(fn.functionName, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        fn.runtime?.let { InfoChip(it, runtimeColor(it)) }
                        fn.state?.let { StatusBadge(it) }
                    }
                }
            }
            HorizontalDivider(color = BorderColor)

            fn.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Spacer(Modifier.height(8.dp))
                Text(desc, fontSize = 13.sp, color = TextSecondary, lineHeight = 19.sp)
                HorizontalDivider(color = BorderColor, modifier = Modifier.padding(top = 8.dp))
            }

            DetailRow("ARN",           fn.functionArn, mono = true)
            DetailRow("Handler",       fn.handler, mono = true)
            DetailRow("Memory",        fn.memorySize?.let { "$it MB" })
            DetailRow("Timeout",       fn.timeout?.let { "$it seconds" })
            DetailRow("Code Size",     fn.codeSize.formatBytes())
            DetailRow("State",         fn.state)
            DetailRow("Package Type",  "Zip / Container")
            DetailRow("Last Modified", fn.lastModified?.formatDate())
        }
    }
}
