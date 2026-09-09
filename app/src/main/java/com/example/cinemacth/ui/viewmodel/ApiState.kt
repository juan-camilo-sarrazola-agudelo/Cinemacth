package com.example.cinemacth.ui.viewmodel

sealed class ApiState<out T> {
    object Idle : ApiState<Nothing>()
    object Loading : ApiState<Nothing>()
    data class Success<T>(
        val data: T,
        val statusCode: Int,
        val url: String,
        val method: String,
        val responseTime: Long,
        val requestBody: String? = null
    ) : ApiState<T>()
    data class Error(val message: String, val statusCode: Int? = null) : ApiState<Nothing>()
}
