package com.cloudops.mobile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudops.mobile.data.api.NetworkClient
import com.cloudops.mobile.data.models.*
import com.cloudops.mobile.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MainViewModel(private val context: Context) : ViewModel() {

    private val prefs = UserPreferencesRepository(context)
    private val api = NetworkClient.api

    val token: StateFlow<String?> = prefs.tokenFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val userName: StateFlow<String?> = prefs.userNameFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val userEmail: StateFlow<String?> = prefs.userEmailFlow.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _awsStatus = MutableStateFlow(UiState<AwsStatusResponse>())
    val awsStatus: StateFlow<UiState<AwsStatusResponse>> = _awsStatus

    private val _ec2Summary = MutableStateFlow(UiState<EC2Summary>())
    val ec2Summary: StateFlow<UiState<EC2Summary>> = _ec2Summary

    private val _instances = MutableStateFlow(UiState<EC2ListResponse>())
    val instances: StateFlow<UiState<EC2ListResponse>> = _instances

    private val _buckets = MutableStateFlow(UiState<S3ListResponse>())
    val buckets: StateFlow<UiState<S3ListResponse>> = _buckets

    private val _currentCost = MutableStateFlow(UiState<CostResponse>())
    val currentCost: StateFlow<UiState<CostResponse>> = _currentCost

    private val _costTrend = MutableStateFlow(UiState<CostTrendResponse>())
    val costTrend: StateFlow<UiState<CostTrendResponse>> = _costTrend

    private val _budgets = MutableStateFlow(UiState<BudgetsResponse>())
    val budgets: StateFlow<UiState<BudgetsResponse>> = _budgets

    private val _securityPosture = MutableStateFlow(UiState<SecurityPosture>())
    val securityPosture: StateFlow<UiState<SecurityPosture>> = _securityPosture

    private val _cpuMetrics = MutableStateFlow(UiState<CpuMetricResponse>())
    val cpuMetrics: StateFlow<UiState<CpuMetricResponse>> = _cpuMetrics

    private fun bearerToken() = "Bearer ${token.value}"

    // Auth
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    prefs.saveAuthData(body.token, body.user.name, body.user.email, body.user.id)
                    onSuccess()
                } else {
                    onError("Invalid email or password")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.register(RegisterRequest(name, email, password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    prefs.saveAuthData(body.token, body.user.name, body.user.email, body.user.id)
                    onSuccess()
                } else {
                    onError("Registration failed. Email may already be in use.")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch { prefs.clearAuthData() }
    }

    // AWS Connect
    fun connectAws(
        accessKeyId: String, secretAccessKey: String, region: String,
        onSuccess: (String) -> Unit, onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.connectAws(
                    bearerToken(),
                    AwsConnectRequest(accessKeyId, secretAccessKey, region)
                )
                if (response.isSuccessful) {
                    val body = response.body()!!
                    onSuccess("Connected to account ${body.account.accountId}")
                    fetchAwsStatus()
                } else {
                    onError("Invalid AWS credentials. Please verify and try again.")
                }
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }
    }

    fun fetchAwsStatus() {
        viewModelScope.launch {
            _awsStatus.value = UiState(isLoading = true)
            try {
                val response = api.getAwsStatus(bearerToken())
                if (response.isSuccessful) {
                    _awsStatus.value = UiState(data = response.body())
                } else {
                    _awsStatus.value = UiState(error = "Failed to fetch AWS status")
                }
            } catch (e: Exception) {
                _awsStatus.value = UiState(error = e.message)
            }
        }
    }

    fun disconnectAws(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                api.disconnectAws(bearerToken())
                _awsStatus.value = UiState(data = AwsStatusResponse(false, null, null, null))
                onDone()
            } catch (e: Exception) {
                onDone()
            }
        }
    }

    // EC2
    fun fetchEC2Summary() {
        viewModelScope.launch {
            _ec2Summary.value = UiState(isLoading = true)
            try {
                val response = api.getEC2Summary(bearerToken())
                if (response.isSuccessful) _ec2Summary.value = UiState(data = response.body())
                else _ec2Summary.value = UiState(error = "Failed to load EC2 data")
            } catch (e: Exception) {
                _ec2Summary.value = UiState(error = e.message)
            }
        }
    }

    fun fetchInstances() {
        viewModelScope.launch {
            _instances.value = UiState(isLoading = true)
            try {
                val response = api.getInstances(bearerToken())
                if (response.isSuccessful) _instances.value = UiState(data = response.body())
                else _instances.value = UiState(error = "Failed to load instances")
            } catch (e: Exception) {
                _instances.value = UiState(error = e.message)
            }
        }
    }

    // S3
    fun fetchBuckets() {
        viewModelScope.launch {
            _buckets.value = UiState(isLoading = true)
            try {
                val response = api.getBuckets(bearerToken())
                if (response.isSuccessful) _buckets.value = UiState(data = response.body())
                else _buckets.value = UiState(error = "Failed to load S3 buckets")
            } catch (e: Exception) {
                _buckets.value = UiState(error = e.message)
            }
        }
    }

    // Cost
    fun fetchCurrentCost() {
        viewModelScope.launch {
            _currentCost.value = UiState(isLoading = true)
            try {
                val response = api.getCurrentCost(bearerToken())
                if (response.isSuccessful) _currentCost.value = UiState(data = response.body())
                else _currentCost.value = UiState(error = "Failed to load cost data")
            } catch (e: Exception) {
                _currentCost.value = UiState(error = e.message)
            }
        }
    }

    fun fetchCostTrend() {
        viewModelScope.launch {
            _costTrend.value = UiState(isLoading = true)
            try {
                val response = api.getCostTrend(bearerToken())
                if (response.isSuccessful) _costTrend.value = UiState(data = response.body())
                else _costTrend.value = UiState(error = "Failed to load cost trend")
            } catch (e: Exception) {
                _costTrend.value = UiState(error = e.message)
            }
        }
    }

    fun fetchBudgets() {
        viewModelScope.launch {
            _budgets.value = UiState(isLoading = true)
            try {
                val response = api.getBudgets(bearerToken())
                if (response.isSuccessful) _budgets.value = UiState(data = response.body())
                else _budgets.value = UiState(error = "Failed to load budgets")
            } catch (e: Exception) {
                _budgets.value = UiState(error = e.message)
            }
        }
    }

    // Security
    fun fetchSecurityPosture() {
        viewModelScope.launch {
            _securityPosture.value = UiState(isLoading = true)
            try {
                val response = api.getSecurityPosture(bearerToken())
                if (response.isSuccessful) _securityPosture.value = UiState(data = response.body())
                else _securityPosture.value = UiState(error = "Failed to load security data")
            } catch (e: Exception) {
                _securityPosture.value = UiState(error = e.message)
            }
        }
    }

    // CloudWatch
    fun fetchCpuMetrics(instanceId: String) {
        viewModelScope.launch {
            _cpuMetrics.value = UiState(isLoading = true)
            try {
                val response = api.getCpuMetrics(bearerToken(), instanceId)
                if (response.isSuccessful) _cpuMetrics.value = UiState(data = response.body())
                else _cpuMetrics.value = UiState(error = "Failed to load metrics")
            } catch (e: Exception) {
                _cpuMetrics.value = UiState(error = e.message)
            }
        }
    }
}
