package com.arescaperoom.domain.rooms

import com.arescaperoom.infrastructure.rooms.RoomDoc

interface IRoomRepository {
    suspend fun createRoom(room: RoomDoc): Boolean
    suspend fun getRoomByCode(roomCode: String): RoomDoc?
    suspend fun updateRoomGuest(roomCode: String, guestEmail: String): Boolean
}