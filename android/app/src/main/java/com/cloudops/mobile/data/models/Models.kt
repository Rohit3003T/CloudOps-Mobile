package com.cloudops.mobile.data.models

// Auth
data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val message: String, val token: String, val user: UserInfo)
data class UserInfo(val id: String, val email: String, val name: String)

// AWS Connect
data class AwsConnectRequest(
    val accessKeyId: String,
    val secretAccessKey: String,
    val region: String
)

data class AwsConnectResponse(
    val message: String,
    val account: AwsAccount
)

data class AwsAccount(val accountId: String, val arn: String, val region: String)

data class AwsStatusResponse(
    val connected: Boolean,
    val accountId: String?,
    val region: String?,
    val arn: String?
)

// EC2
data class EC2Instance(
    val instanceId: String,
    val name: String,
    val state: String,
    val instanceType: String,
    val publicIp: String?,
    val privateIp: String?,
    val az: String?,
    val launchTime: String?,
    val platform: String
)

data class EC2ListResponse(val instances: List<EC2Instance>, val total: Int)

data class EC2Summary(
    val total: Int,
    val running: Int,
    val stopped: Int,
    val other: Int
)

// S3
data class S3Bucket(
    val name: String,
    val createdAt: String?,
    val region: String,
    val isPublic: Boolean
)

data class S3ListResponse(val buckets: List<S3Bucket>, val total: Int)

// CloudWatch
data class CpuDataPoint(
    val timestamp: String,
    val average: Double,
    val maximum: Double
)

data class CpuMetricResponse(
    val instanceId: String,
    val metric: String,
    val datapoints: List<CpuDataPoint>
)

// Cost
data class ServiceCost(val service: String, val cost: String, val unit: String?)

data class CostResponse(
    val period: Map<String, String>,
    val totalCost: String,
    val currency: String,
    val byService: List<ServiceCost>
)

data class MonthCost(val period: String, val cost: String, val unit: String)
data class CostTrendResponse(val trend: List<MonthCost>)

data class BudgetLimit(val amount: String?, val unit: String?)
data class BudgetSpend(val amount: String?, val unit: String?)
data class Budget(
    val name: String,
    val type: String?,
    val limit: BudgetLimit,
    val actual: BudgetSpend?,
    val forecast: BudgetSpend?,
    val timeUnit: String?
)
data class BudgetsResponse(val budgets: List<Budget>, val total: Int)

// Security
data class SecurityFinding(
    val type: String,
    val severity: String,
    val resource: String,
    val message: String
)

data class SecurityPosture(
    val score: Int,
    val posture: String,
    val findings: List<SecurityFinding>,
    val critical: Int,
    val high: Int,
    val medium: Int,
    val low: Int
)

// Generic error
data class ErrorResponse(val error: String)
