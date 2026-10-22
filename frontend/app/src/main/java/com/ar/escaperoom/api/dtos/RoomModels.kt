package com.ar.escaperoom.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(
    val hostEmail: String
)

@Serializable
data class JoinRoomRequest(
    val guestEmail: String,
    val roomCode: String
)

@Serializable
data class RoomResponse(
    val roomCode: String,
    val hostEmail: String,
    val guestEmail: String? = null,
    val status: RoomStatus,
    val message: String
)

@Serializable
enum class RoomStatus {
    WAITING,
    PLAYING
}