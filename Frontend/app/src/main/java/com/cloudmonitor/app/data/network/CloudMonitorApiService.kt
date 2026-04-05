package com.cloudmonitor.app.data.network

import com.cloudmonitor.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface CloudMonitorApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ── Health ────────────────────────────────────────────────────────────────
    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    // ── EC2 ───────────────────────────────────────────────────────────────────
    @GET("api/ec2")
    suspend fun getEc2Instances(
        @Header("Authorization") token: String,
        @Query("state") state: String? = null
    ): Response<Ec2Response>

    // ── S3 ────────────────────────────────────────────────────────────────────
    @GET("api/s3")
    suspend fun getS3Buckets(
        @Header("Authorization") token: String
    ): Response<S3Response>

    // ── Lambda ────────────────────────────────────────────────────────────────
    @GET("api/lambda")
    suspend fun getLambdaFunctions(
        @Header("Authorization") token: String
    ): Response<LambdaResponse>

    // ── EBS ───────────────────────────────────────────────────────────────────
    @GET("api/ebs")
    suspend fun getEbsVolumes(
        @Header("Authorization") token: String,
        @Query("state") state: String? = null
    ): Response<EbsResponse>

    // ── VPC ───────────────────────────────────────────────────────────────────
    @GET("api/vpc")
    suspend fun getVpcs(
        @Header("Authorization") token: String
    ): Response<VpcResponse>

    @GET("api/vpc/subnets")
    suspend fun getSubnets(
        @Header("Authorization") token: String,
        @Query("vpcId") vpcId: String? = null
    ): Response<SubnetResponse>

    @GET("api/vpc/security-groups")
    suspend fun getSecurityGroups(
        @Header("Authorization") token: String,
        @Query("vpcId") vpcId: String? = null
    ): Response<SecurityGroupResponse>

    // ── Monitoring ────────────────────────────────────────────────────────────
    @GET("api/monitoring/ec2/{instanceId}")
    suspend fun getMonitoring(
        @Header("Authorization") token: String,
        @Path("instanceId") instanceId: String,
        @Query("period") period: Int = 300,
        @Query("hoursBack") hoursBack: Int = 3
    ): Response<MonitoringResponse>

    // ── Cost ──────────────────────────────────────────────────────────────────
    @GET("api/cost")
    suspend fun getCost(
        @Header("Authorization") token: String,
        @Query("granularity") granularity: String = "MONTHLY",
        @Query("months") months: Int = 1
    ): Response<CostResponse>

    // ── CI/CD ─────────────────────────────────────────────────────────────────
    @GET("api/cicd/runs")
    suspend fun getCicdRuns(
        @Header("Authorization") token: String,
        @Query("owner") owner: String? = null,
        @Query("repo") repo: String? = null,
        @Query("perPage") perPage: Int = 15
    ): Response<CicdRunsResponse>

}
