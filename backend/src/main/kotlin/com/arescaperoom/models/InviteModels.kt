package com.arescaperoom.models

import kotlinx.serialization.Serializable

@Serializable
data class SendInviteRequest(
    val fromPlayerId: String,
    val fromPlayerName: String,
    val toPlayerId: String,
    val toPlayerName: String = "",
    val roomCode: String
)

@Serializable
data class InviteResponse(
    val id: String,
    val fromPlayerId: String,
    val fromPlayerName: String,
    val toPlayerId: String,
    val toPlayerName: String = "",
    val roomCode: String,
    val status: String,
    val expiresAt: Long,
    val createdAt: Long
)

@Serializable
data class AcceptInviteRequest(
    val roomCode: String
)

@Serializable
data class AcceptInviteResponse(
    val roomCode: String,
    val fromPlayerName: String
)
