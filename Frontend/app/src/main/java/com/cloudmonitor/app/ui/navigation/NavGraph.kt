package com.cloudmonitor.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cloudmonitor.app.ui.screens.*
import com.cloudmonitor.app.ui.theme.*
import com.cloudmonitor.app.viewmodel.CloudMonitorViewModel

@Composable
fun AppNavGraph(viewModel: CloudMonitorViewModel) {
    val navController = rememberNavController()
    val token by viewModel.token.collectAsState()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val isLoggedIn = !token.isNullOrBlank()
    val showBottomBar = currentRoute != Screen.Login.route

    // React to auth state changes
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn && currentRoute != Screen.Login.route) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = AwsDark,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(viewModel = viewModel, onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Dashboard.route)  { DashboardScreen(viewModel) }
            composable(Screen.EC2.route)        { EC2Screen(viewModel) }
            composable(Screen.S3.route)         { S3Screen(viewModel) }
            composable(Screen.Lambda.route)     { LambdaScreen(viewModel) }
            composable(Screen.EBS.route)        { EBSScreen(viewModel) }
            composable(Screen.VPC.route)        { VPCScreen(viewModel) }
            composable(Screen.Monitoring.route) { MonitoringScreen(viewModel) }
            composable(Screen.Cost.route)       { CostScreen(viewModel) }
            composable(Screen.CICD.route)       { CICDScreen(viewModel) }
        }
    }
}

@Composable
fun AppBottomBar(currentRoute: String?, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        containerColor = AwsDarkSurface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(64.dp)
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(item.label, fontSize = 9.sp, maxLines = 1)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = AwsOrange,
                    selectedTextColor   = AwsOrange,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary,
                    indicatorColor      = Color(0x1FFF9900)
                )
            )
        }
    }
}
