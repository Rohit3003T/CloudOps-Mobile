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
fun RegisterScreen(
    viewModel: MainViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AwsDark, AwsDarkSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(AwsOrange.copy(alpha = 0.15f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Cloud, null, tint = AwsOrange, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(16.dp))
            Text("Create Account", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AwsLightText)
            Text("Join CloudOps Mobile", fontSize = 14.sp, color = AwsGray)
            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AwsDarkCard),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    listOf(
                        Triple(name, "Full Name", Icons.Default.Person),
                    ).forEach { /* handled below */ }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = AwsGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange, focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange, unfocusedBorderColor = AwsGray.copy(alpha = 0.5f),
                            unfocusedLabelColor = AwsGray
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = AwsGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange, focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange, unfocusedBorderColor = AwsGray.copy(alpha = 0.5f),
                            unfocusedLabelColor = AwsGray
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password (min 6 chars)") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = AwsGray) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = AwsGray)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange, focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange, unfocusedBorderColor = AwsGray.copy(alpha = 0.5f),
                            unfocusedLabelColor = AwsGray
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.LockOpen, null, tint = AwsGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AwsOrange, focusedLabelColor = AwsOrange,
                            cursorColor = AwsOrange, unfocusedBorderColor = AwsGray.copy(alpha = 0.5f),
                            unfocusedLabelColor = AwsGray
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
                            when {
                                name.isBlank() -> errorMessage = "Please enter your full name"
                                email.isBlank() -> errorMessage = "Please enter your email"
                                password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                                password != confirmPassword -> errorMessage = "Passwords do not match"
                                else -> {
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.register(
                                        name.trim(), email.trim(), password,
                                        onSuccess = { isLoading = false; onRegisterSuccess() },
                                        onError = { isLoading = false; errorMessage = it }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AwsOrange)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = AwsDark, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AwsDark)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = AwsGray, fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = AwsOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
