package com.ar.escaperoom.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class GameSessionResponse(
    val id: String = "",
    val playerId: String = "",
    val partnerName: String = "",
    val roomId: String = "",
    val durationSeconds: Int = 0,
    val hintsUsed: Int = 0,
    val chatMessages: Int = 0,
    val voiceSeconds: Int = 0,
    val completedAt: String = ""
)

@Serializable
data class LeaderboardEntryResponse(
    val playerId: String = "",
    val playerName: String = "",
    val partnerName: String = "",
    val durationSeconds: Int = 0,
    val hintsUsed: Int = 0,
    val voiceSeconds: Int = 0,
    val chatMessages: Int = 0,
    val completedAt: String = ""
)

@Serializable
data class UserSearchResult(
    val email: String = "",
    val username: String = "",
    val profilePicture: String? = null
)

@Serializable
data class ApiListResponse<T>(
    val success: Boolean = false,
    val data: List<T>? = null,
    val message: String? = null
)

@Serializable
data class ApiSingleResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
    val message: String? = null
)
