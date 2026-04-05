package com.cloudmonitor.app.data.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

/**
 * Safely executes a suspend API call and wraps it in ApiResult.
 * Catches network errors and HTTP error codes.
 */
suspend fun <T> safeApiCall(call: suspend () -> retrofit2.Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error("Empty response body", response.code())
            }
        } else {
            ApiResult.Error(
                message = response.message().ifBlank { "HTTP ${response.code()}" },
                code = response.code()
            )
        }
    } catch (e: java.net.SocketTimeoutException) {
        ApiResult.Error("Connection timed out. Is the backend running?")
    } catch (e: java.net.ConnectException) {
        ApiResult.Error("Cannot connect to server. Check the BASE_URL in build.gradle.kts.")
    } catch (e: Exception) {
        ApiResult.Error(e.localizedMessage ?: "Unknown error")
    }
}
