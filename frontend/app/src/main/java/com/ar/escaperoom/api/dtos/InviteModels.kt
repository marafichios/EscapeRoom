package com.ar.escaperoom.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class InviteResponse(
    val id: String = "",
    val fromPlayerId: String = "",
    val fromPlayerName: String = "",
    val toPlayerId: String = "",
    val toPlayerName: String = "",
    val roomCode: String = "",
    val status: String = "",
    val expiresAt: Long = 0L,
    val createdAt: Long = 0L
)

@Serializable
data class SendInviteRequest(
    val fromPlayerId: String,
    val fromPlayerName: String,
    val toPlayerId: String,
    val toPlayerName: String = "",
    val roomCode: String
)

@Serializable
data class AcceptInviteRequest(
    val roomCode: String
)

@Serializable
data class AcceptInviteResponse(
    val roomCode: String = "",
    val fromPlayerName: String = ""
)
