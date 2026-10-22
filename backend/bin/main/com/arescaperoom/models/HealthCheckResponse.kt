package com.arescaperoom.models

import kotlinx.serialization.Serializable

@Serializable
data class HealthCheckResponse(
    val status: String,
    val service: String? = null,
    val version: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
