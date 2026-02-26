package com.cloudops.mobile.data.api

import com.cloudops.mobile.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface CloudOpsApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserInfo>

    // AWS
    @POST("api/aws/connect")
    suspend fun connectAws(
        @Header("Authorization") token: String,
        @Body request: AwsConnectRequest
    ): Response<AwsConnectResponse>

    @GET("api/aws/status")
    suspend fun getAwsStatus(@Header("Authorization") token: String): Response<AwsStatusResponse>

    @DELETE("api/aws/disconnect")
    suspend fun disconnectAws(@Header("Authorization") token: String): Response<Map<String, String>>

    // EC2
    @GET("api/ec2/instances")
    suspend fun getInstances(@Header("Authorization") token: String): Response<EC2ListResponse>

    @GET("api/ec2/summary")
    suspend fun getEC2Summary(@Header("Authorization") token: String): Response<EC2Summary>

    // S3
    @GET("api/s3/buckets")
    suspend fun getBuckets(@Header("Authorization") token: String): Response<S3ListResponse>

    // CloudWatch
    @GET("api/cloudwatch/cpu/{instanceId}")
    suspend fun getCpuMetrics(
        @Header("Authorization") token: String,
        @Path("instanceId") instanceId: String
    ): Response<CpuMetricResponse>

    // Cost
    @GET("api/cost/current")
    suspend fun getCurrentCost(@Header("Authorization") token: String): Response<CostResponse>

    @GET("api/cost/trend")
    suspend fun getCostTrend(@Header("Authorization") token: String): Response<CostTrendResponse>

    @GET("api/cost/budgets")
    suspend fun getBudgets(@Header("Authorization") token: String): Response<BudgetsResponse>

    // Security
    @GET("api/security/posture")
    suspend fun getSecurityPosture(@Header("Authorization") token: String): Response<SecurityPosture>
}
