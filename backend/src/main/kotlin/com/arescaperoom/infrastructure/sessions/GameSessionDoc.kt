package com.arescaperoom.infrastructure.sessions

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class GameSessionDoc(
    @BsonId
    val id: ObjectId = ObjectId(),
    val playerId: String,
    val playerName: String = "",
    val partnerName: String = "unknown",
    val partnerId: String = "",
    val matchId: String = "",
    val roomId: String,
    val durationSeconds: Int,
    val hintsUsed: Int,
    val chatMessages: Int,
    val voiceSeconds: Int,
    val completedAt: String
)
