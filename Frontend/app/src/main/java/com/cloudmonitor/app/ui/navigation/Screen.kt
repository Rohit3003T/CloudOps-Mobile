package com.cloudmonitor.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object Dashboard  : Screen("dashboard")
    object EC2        : Screen("ec2")
    object S3         : Screen("s3")
    object Lambda     : Screen("lambda")
    object EBS        : Screen("ebs")
    object VPC        : Screen("vpc")
    object Monitoring : Screen("monitoring")
    object Cost       : Screen("cost")
    object CICD       : Screen("cicd")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard,  "Dashboard",  Icons.Default.Dashboard),
    BottomNavItem(Screen.EC2,        "EC2",        Icons.Default.Computer),
    BottomNavItem(Screen.S3,         "S3",         Icons.Default.Storage),
    BottomNavItem(Screen.Lambda,     "Lambda",     Icons.Default.Code),
    BottomNavItem(Screen.EBS,        "EBS",        Icons.Default.DataUsage),
    BottomNavItem(Screen.VPC,        "VPC",        Icons.Default.Hub),
    BottomNavItem(Screen.Monitoring, "Monitor",    Icons.Default.BarChart),
    BottomNavItem(Screen.Cost,       "Cost",       Icons.Default.AttachMoney),
    BottomNavItem(Screen.CICD,       "CI/CD",      Icons.Default.Loop),
)
