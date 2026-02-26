package com.cloudops.mobile.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object ConnectAws : Screen("connect_aws")
    object EC2 : Screen("ec2")
    object S3 : Screen("s3")
    object CloudWatch : Screen("cloudwatch/{instanceId}") {
        fun createRoute(instanceId: String) = "cloudwatch/$instanceId"
    }
    object Cost : Screen("cost")
    object Security : Screen("security")
    object Profile : Screen("profile")
}
