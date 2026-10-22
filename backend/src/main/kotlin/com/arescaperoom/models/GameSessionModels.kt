package com.arescaperoom.models

import kotlinx.serialization.Serializable


@Serializable
data class GameSessionRequest(
    val playerId: String,
    val playerName: String = "",
    val partnerName: String = "unknown",
    val partnerId: String = "",   // emailul partener
    val matchId: String = "",     // cod camera Photon
    val roomId: String,
    val durationSeconds: Int,
    val hintsUsed: Int,
    val chatMessages: Int,
    val voiceSeconds: Int,
    val completedAt: String
)


@Serializable
data class GameSessionResponse(
    val id: String,
    val playerId: String,
    val partnerName: String,
    val roomId: String,
    val durationSeconds: Int,
    val hintsUsed: Int,
    val chatMessages: Int,
    val voiceSeconds: Int,
    val completedAt: String
)

@Serializable
data class LeaderboardEntryResponse(
    val playerId: String,
    val playerName: String = "",
    val partnerName: String,
    val durationSeconds: Int,
    val hintsUsed: Int,
    val voiceSeconds: Int = 0,
    val chatMessages: Int = 0,
    val completedAt: String
)
