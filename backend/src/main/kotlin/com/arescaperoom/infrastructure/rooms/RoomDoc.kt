package com.arescaperoom.infrastructure.rooms

import com.arescaperoom.models.RoomStatus
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class RoomDoc(
    @BsonId
    val id: ObjectId = ObjectId(),
    val roomCode: String,
    val hostEmail: String,
    val guestEmail: String? = null,
    val status: String = RoomStatus.WAITING.toString()  // se schimba in PLAYING la join
)