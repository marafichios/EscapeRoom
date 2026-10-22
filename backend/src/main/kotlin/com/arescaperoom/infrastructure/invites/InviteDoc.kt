package com.arescaperoom.infrastructure.invites

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.Date

enum class InviteStatus { PENDING, ACCEPTED, DECLINED, CANCELLED, JOINED }

data class InviteDoc(
    @BsonId
    val id: ObjectId = ObjectId(),
    val fromPlayerId: String,
    val fromPlayerName: String,
    val toPlayerId: String,
    val toPlayerName: String = "",
    val roomCode: String,
    val status: String = InviteStatus.PENDING.name,
    // TTL index
    val expiresAt: Date = Date(System.currentTimeMillis() + 10 * 60 * 1000L), // 10 minute
    val createdAt: Date = Date()
)
