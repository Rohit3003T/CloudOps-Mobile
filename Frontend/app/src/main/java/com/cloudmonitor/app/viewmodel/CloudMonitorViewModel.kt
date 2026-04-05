package com.cloudmonitor.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cloudmonitor.app.data.model.*
import com.cloudmonitor.app.data.network.ApiResult
import com.cloudmonitor.app.data.network.RetrofitClient
import com.cloudmonitor.app.data.network.TokenManager
import com.cloudmonitor.app.data.repository.CloudMonitorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Generic UI state wrapper
sealed class UiState<out T> {
    object Idle    : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T, val isMock: Boolean = false) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class CloudMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repository   = CloudMonitorRepository(RetrofitClient.apiService, tokenManager)

    // ── Auth ──────────────────────────────────────────────────────────────────
    val token: StateFlow<String?> = tokenManager.tokenFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val username: StateFlow<String?> = tokenManager.usernameFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _loginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = repository.login(username, password)) {
                is ApiResult.Success -> {
                    val body = result.data
                    if (body.token != null) {
                        repository.saveToken(body.token, body.user?.username ?: username)
                        _loginState.value = UiState.Success(body)
                    } else {
                        _loginState.value = UiState.Error(body.error ?: "Login failed")
                    }
                }
                is ApiResult.Error   -> _loginState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun resetLogin() { _loginState.value = UiState.Idle }

    fun logout() {
        viewModelScope.launch { repository.logout() }
        resetAll()
    }

    // ── Health ────────────────────────────────────────────────────────────────
    private val _health = MutableStateFlow<UiState<HealthResponse>>(UiState.Idle)
    val health: StateFlow<UiState<HealthResponse>> = _health.asStateFlow()

    fun loadHealth() = collectFlow(repository.getHealth(), _health)

    // ── EC2 ───────────────────────────────────────────────────────────────────
    private val _ec2 = MutableStateFlow<UiState<Ec2Response>>(UiState.Idle)
    val ec2: StateFlow<UiState<Ec2Response>> = _ec2.asStateFlow()

    fun loadEc2(state: String? = null) = collectFlow(repository.getEc2Instances(state), _ec2) { it.mock == true }

    // ── S3 ────────────────────────────────────────────────────────────────────
    private val _s3 = MutableStateFlow<UiState<S3Response>>(UiState.Idle)
    val s3: StateFlow<UiState<S3Response>> = _s3.asStateFlow()

    fun loadS3() = collectFlow(repository.getS3Buckets(), _s3) { it.mock == true }

    // ── Lambda ────────────────────────────────────────────────────────────────
    private val _lambda = MutableStateFlow<UiState<LambdaResponse>>(UiState.Idle)
    val lambda: StateFlow<UiState<LambdaResponse>> = _lambda.asStateFlow()

    fun loadLambda() = collectFlow(repository.getLambdaFunctions(), _lambda) { it.mock == true }

    // ── EBS ───────────────────────────────────────────────────────────────────
    private val _ebs = MutableStateFlow<UiState<EbsResponse>>(UiState.Idle)
    val ebs: StateFlow<UiState<EbsResponse>> = _ebs.asStateFlow()

    fun loadEbs() = collectFlow(repository.getEbsVolumes(), _ebs) { it.mock == true }

    // ── VPC ───────────────────────────────────────────────────────────────────
    private val _vpcs = MutableStateFlow<UiState<VpcResponse>>(UiState.Idle)
    private val _subnets = MutableStateFlow<UiState<SubnetResponse>>(UiState.Idle)
    private val _sgs     = MutableStateFlow<UiState<SecurityGroupResponse>>(UiState.Idle)
    val vpcs:    StateFlow<UiState<VpcResponse>>           = _vpcs.asStateFlow()
    val subnets: StateFlow<UiState<SubnetResponse>>        = _subnets.asStateFlow()
    val sgs:     StateFlow<UiState<SecurityGroupResponse>> = _sgs.asStateFlow()

    fun loadVpc() {
        collectFlow(repository.getVpcs(), _vpcs) { it.mock == true }
        collectFlow(repository.getSubnets(), _subnets)
        collectFlow(repository.getSecurityGroups(), _sgs)
    }

    // ── Monitoring ────────────────────────────────────────────────────────────
    private val _monitoring = MutableStateFlow<UiState<MonitoringResponse>>(UiState.Idle)
    val monitoring: StateFlow<UiState<MonitoringResponse>> = _monitoring.asStateFlow()

    private val _monitoringInstanceId = MutableStateFlow("i-0abc123def456789a")
    val monitoringInstanceId: StateFlow<String> = _monitoringInstanceId.asStateFlow()

    fun setMonitoringInstance(id: String) { _monitoringInstanceId.value = id }

    fun loadMonitoring(instanceId: String, period: Int = 300, hoursBack: Int = 3) =
        collectFlow(repository.getMonitoring(instanceId, period, hoursBack), _monitoring) { it.mock == true }

    // ── Cost ──────────────────────────────────────────────────────────────────
    private val _cost = MutableStateFlow<UiState<CostResponse>>(UiState.Idle)
    val cost: StateFlow<UiState<CostResponse>> = _cost.asStateFlow()

    fun loadCost() = collectFlow(repository.getCost(), _cost) { it.mock == true }

    // ── CI/CD ─────────────────────────────────────────────────────────────────
    private val _cicd = MutableStateFlow<UiState<CicdRunsResponse>>(UiState.Idle)
    val cicd: StateFlow<UiState<CicdRunsResponse>> = _cicd.asStateFlow()

    private val _cicdOwner = MutableStateFlow("")
    private val _cicdRepo  = MutableStateFlow("")
    val cicdOwner: StateFlow<String> = _cicdOwner.asStateFlow()
    val cicdRepo:  StateFlow<String> = _cicdRepo.asStateFlow()

    fun setCicdOwner(v: String) { _cicdOwner.value = v }
    fun setCicdRepo(v: String)  { _cicdRepo.value  = v }

    fun loadCicd(owner: String? = null, repo: String? = null) =
        collectFlow(repository.getCicdRuns(owner, repo), _cicd)

    // ── Helper ────────────────────────────────────────────────────────────────
    private fun <T> collectFlow(
        flow: Flow<ApiResult<T>>,
        state: MutableStateFlow<UiState<T>>,
        isMock: ((T) -> Boolean)? = null
    ) {
        viewModelScope.launch {
            flow.collect { result ->
                state.value = when (result) {
                    is ApiResult.Loading  -> UiState.Loading
                    is ApiResult.Success  -> UiState.Success(result.data, isMock?.invoke(result.data) ?: false)
                    is ApiResult.Error    -> UiState.Error(result.message)
                }
            }
        }
    }

    private fun resetAll() {
        _health.value = UiState.Idle
        _ec2.value = UiState.Idle
        _s3.value = UiState.Idle
        _lambda.value = UiState.Idle
        _ebs.value = UiState.Idle
        _vpcs.value = UiState.Idle
        _subnets.value = UiState.Idle
        _sgs.value = UiState.Idle
        _monitoring.value = UiState.Idle
        _cost.value = UiState.Idle
        _cicd.value = UiState.Idle
    }
}
