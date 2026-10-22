package com.arescaperoom.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ErrorResponse(
    val error: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
