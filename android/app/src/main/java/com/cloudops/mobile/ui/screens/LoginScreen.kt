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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudops.mobile.ui.theme.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(AwsDark, AwsDarkSurface))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo area
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AwsOrange.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cloud,
                    contentDescription = null,
                    tint = AwsOrange,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "CloudOps Mobile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AwsLightText
            )
            Text(
                "Cloud Operations Dashboard",
                fontSize = 14.sp,
                color = AwsGray
            )

            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AwsDarkCard),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Sign In", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AwsLightText)

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = AwsGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange,
                            focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange,
                            unfocusedBorderColor = AwsGray.copy(alpha = 0.5f),
                            unfocusedLabelColor = AwsGray,
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = AwsGray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null, tint = AwsGray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange,
                            focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange,
                            unfocusedBorderColor = AwsGray.copy(alpha = 0.5f),
                            unfocusedLabelColor = AwsGray,
                        ),
                        singleLine = true
                    )

                    AnimatedVisibility(errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AwsRed.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, null, tint = AwsRed, modifier = Modifier.size(16.dp))
                                Text(errorMessage ?: "", color = AwsRed, fontSize = 13.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter email and password"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            viewModel.login(
                                email.trim(), password,
                                onSuccess = { isLoading = false; onLoginSuccess() },
                                onError = { isLoading = false; errorMessage = it }
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
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AwsDark)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = AwsGray, fontSize = 14.sp)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Create Account", color = AwsOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
