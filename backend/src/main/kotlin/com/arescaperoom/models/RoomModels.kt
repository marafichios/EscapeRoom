package com.arescaperoom.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(val hostEmail: String) // cine creeaza camera

@Serializable
data class JoinRoomRequest(val guestEmail: String, val roomCode: String) // cine da join in camera si unde

@Serializable
data class RoomResponse(
    val roomCode: String,
    val hostEmail: String,
    val guestEmail: String? = null,
    val status: RoomStatus, // "WAITING" sau "PLAYING"
    val message: String
)

enum class RoomStatus {
    WAITING,
    PLAYING
}