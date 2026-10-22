package com.arescaperoom.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class User(
    val email: String,
    val username: String,
    val profilePicture: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val gameProgress: GameProgress = GameProgress()
)

@Serializable
data class GameProgress(
    val currentLevel: Int = 1,
    val completedRooms: List<String> = emptyList(),
    val score: Int = 0,
    val achievements: List<String> = emptyList(),
    val lastPlayedAt: Long = System.currentTimeMillis()
)

@Serializable
data class UserRequest(
    val username: String,
    val email: String,
    val profilePicture: String? = null,
)

//@Serializable
//data class UserResponse(
//    val id: String,
//    val username: String,
//    val email: String,
//    val createdAt: Long,
//    val gameProgress: GameProgress?
//)
//
//fun User.toResponse(): UserResponse = UserResponse(
//    username = username,
//    email = email,
//    createdAt = createdAt,
//    gameProgress = gameProgress
//)
