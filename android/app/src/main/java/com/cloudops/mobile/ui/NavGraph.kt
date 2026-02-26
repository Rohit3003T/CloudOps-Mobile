package com.cloudops.mobile.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cloudops.mobile.ui.screens.*
import com.cloudops.mobile.viewmodel.MainViewModel

@Composable
fun CloudOpsNavGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val token by viewModel.token.collectAsState()

    val startDestination = if (token != null) Screen.Dashboard.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigateUp() }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.ConnectAws.route) {
            ConnectAwsScreen(viewModel = viewModel, onBack = { navController.navigateUp() })
        }

        composable(Screen.EC2.route) {
            EC2Screen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
                onViewMetrics = { instanceId ->
                    navController.navigate(Screen.CloudWatch.createRoute(instanceId))
                }
            )
        }

        composable(Screen.S3.route) {
            S3Screen(viewModel = viewModel, onBack = { navController.navigateUp() })
        }

        composable(Screen.CloudWatch.route) { backStackEntry ->
            val instanceId = backStackEntry.arguments?.getString("instanceId") ?: return@composable
            CloudWatchScreen(
                viewModel = viewModel,
                instanceId = instanceId,
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Cost.route) {
            CostScreen(viewModel = viewModel, onBack = { navController.navigateUp() })
        }

        composable(Screen.Security.route) {
            SecurityScreen(viewModel = viewModel, onBack = { navController.navigateUp() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
