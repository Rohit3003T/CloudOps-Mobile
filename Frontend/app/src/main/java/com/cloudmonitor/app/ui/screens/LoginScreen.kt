package com.cloudmonitor.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel
import com.cloudmonitor.app.viewmodel.UiState

@Composable
fun LoginScreen(
    viewModel: CloudMonitorViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsState()
    val focus = LocalFocusManager.current

    var username    by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var showPass    by remember { mutableStateOf(false) }
    var errorText   by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        when (val s = loginState) {
            is UiState.Success -> { errorText = ""; onLoginSuccess() }
            is UiState.Error   -> { errorText = s.message }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(AwsDark, AwsDarkVariant))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo
            Text("☁", fontSize = 56.sp, color = AwsOrange, modifier = Modifier.padding(bottom = 4.dp))
            Text(
                "CloudOps",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )
            Text(
                "AWS Infrastructure Console",
                fontSize = 13.sp,
                color = TextTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
                Text(
                    "  SIGN IN  ", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 0.1.sp, color = TextTertiary,
                    fontFamily = FontFamily.Monospace
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = BorderColor)
            }

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorText = "" },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = AwsOrange, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                colors = loginFieldColors(),
                shape = RoundedCornerShape(10.dp)
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorText = "" },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = AwsOrange, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null, tint = TextTertiary
                        )
                    }
                },
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focus.clearFocus()
                    if (username.isNotBlank() && password.isNotBlank()) {
                        viewModel.login(username.trim(), password)
                    }
                }),
                colors = loginFieldColors(),
                shape = RoundedCornerShape(10.dp)
            )

            // Error banner
            if (errorText.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x1FFF4757), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Red, modifier = Modifier.size(16.dp))
                    Text(errorText, color = Red, fontSize = 13.sp)
                }
            }

            // Sign in button
            Button(
                onClick = {
                    viewModel.resetLogin()
                    viewModel.login(username.trim(), password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = username.isNotBlank() && password.isNotBlank() && loginState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AwsOrange,
                    contentColor   = Color.Black,
                    disabledContainerColor = Color(0xFF5C4200)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (loginState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Sign In", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }

//            // Demo hint
////            Column(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .background(AwsDarkContainer, RoundedCornerShape(8.dp))
////                    .padding(12.dp),
////                verticalArrangement = Arrangement.spacedBy(4.dp)
////            )
//            {
//                Text(
//                    "DEMO CREDENTIALS",
//                    fontSize = 9.sp, fontWeight = FontWeight.Bold,
//                    letterSpacing = 0.1.sp, color = TextTertiary,
//                    fontFamily = FontFamily.Monospace
//                )
////                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
////                    Text("user  admin", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
////                    Text("pass  password123", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
////                }
//            }
        }
    }
}

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = AwsOrange,
    unfocusedBorderColor = BorderColor,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextSecondary,
    cursorColor          = AwsOrange,
    focusedLabelColor    = AwsOrange,
    unfocusedLabelColor  = TextTertiary,
    focusedContainerColor   = AwsDarkContainer,
    unfocusedContainerColor = AwsDarkContainer
)
