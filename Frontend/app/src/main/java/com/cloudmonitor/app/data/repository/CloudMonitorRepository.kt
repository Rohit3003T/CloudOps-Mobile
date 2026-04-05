package com.cloudmonitor.app.data.repository

import com.cloudmonitor.app.data.model.*
import com.cloudmonitor.app.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CloudMonitorRepository(
    private val api: CloudMonitorApiService,
    private val tokenManager: TokenManager
) {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun bearer(): String {
        val token = tokenManager.getToken() ?: ""
        return "Bearer $token"
    }

    private fun <T> apiFlow(call: suspend () -> ApiResult<T>): Flow<ApiResult<T>> = flow {
        emit(ApiResult.Loading)
        emit(call())
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    suspend fun login(username: String, password: String): ApiResult<LoginResponse> =
        safeApiCall { api.login(LoginRequest(username, password)) }

    suspend fun logout() = tokenManager.clearToken()

    val tokenFlow: Flow<String?> get() = tokenManager.tokenFlow
    val usernameFlow: Flow<String?> get() = tokenManager.usernameFlow

    suspend fun saveToken(token: String, username: String) =
        tokenManager.saveToken(token, username)

    // ── Health ────────────────────────────────────────────────────────────────

    fun getHealth(): Flow<ApiResult<HealthResponse>> = apiFlow {
        safeApiCall { api.getHealth() }
    }

    // ── EC2 ───────────────────────────────────────────────────────────────────

    fun getEc2Instances(state: String? = null): Flow<ApiResult<Ec2Response>> = apiFlow {
        safeApiCall { api.getEc2Instances(bearer(), state) }
    }

    // ── S3 ────────────────────────────────────────────────────────────────────

    fun getS3Buckets(): Flow<ApiResult<S3Response>> = apiFlow {
        safeApiCall { api.getS3Buckets(bearer()) }
    }

    // ── Lambda ────────────────────────────────────────────────────────────────

    fun getLambdaFunctions(): Flow<ApiResult<LambdaResponse>> = apiFlow {
        safeApiCall { api.getLambdaFunctions(bearer()) }
    }

    // ── EBS ───────────────────────────────────────────────────────────────────

    fun getEbsVolumes(state: String? = null): Flow<ApiResult<EbsResponse>> = apiFlow {
        safeApiCall { api.getEbsVolumes(bearer(), state) }
    }

    // ── VPC ───────────────────────────────────────────────────────────────────

    fun getVpcs(): Flow<ApiResult<VpcResponse>> = apiFlow {
        safeApiCall { api.getVpcs(bearer()) }
    }

    fun getSubnets(vpcId: String? = null): Flow<ApiResult<SubnetResponse>> = apiFlow {
        safeApiCall { api.getSubnets(bearer(), vpcId) }
    }

    fun getSecurityGroups(vpcId: String? = null): Flow<ApiResult<SecurityGroupResponse>> = apiFlow {
        safeApiCall { api.getSecurityGroups(bearer(), vpcId) }
    }

    // ── Monitoring ────────────────────────────────────────────────────────────

    fun getMonitoring(
        instanceId: String,
        period: Int = 300,
        hoursBack: Int = 3
    ): Flow<ApiResult<MonitoringResponse>> = apiFlow {
        safeApiCall { api.getMonitoring(bearer(), instanceId, period, hoursBack) }
    }

    // ── Cost ──────────────────────────────────────────────────────────────────

    fun getCost(granularity: String = "MONTHLY", months: Int = 1): Flow<ApiResult<CostResponse>> = apiFlow {
        safeApiCall { api.getCost(bearer(), granularity, months) }
    }

    // ── CI/CD ─────────────────────────────────────────────────────────────────

    fun getCicdRuns(
        owner: String? = null,
        repo: String? = null
    ): Flow<ApiResult<CicdRunsResponse>> = apiFlow {
        safeApiCall { api.getCicdRuns(bearer(), owner, repo) }
    }

}
