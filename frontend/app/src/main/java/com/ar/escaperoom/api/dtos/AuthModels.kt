package com.ar.escaperoom.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val idToken: String)

@Serializable
data class AuthResponse(
    val message: String,
    val username: String? = null,
    val email: String? = null,
    val token: String? = null // JWT de sesiune emis de backend, trimis apoi ca Bearer la fiecare request
)