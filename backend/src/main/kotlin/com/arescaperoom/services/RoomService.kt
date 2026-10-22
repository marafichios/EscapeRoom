package com.arescaperoom.services
import com.arescaperoom.domain.rooms.IRoomRepository
import com.arescaperoom.infrastructure.rooms.RoomDoc
import com.arescaperoom.models.RoomResponse
import com.arescaperoom.models.RoomStatus

class RoomService(private val roomRepository: IRoomRepository) {

    suspend fun createRoom(hostEmail: String): RoomResponse {
        // gen cod random camera
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val roomCode = (1..4).map { charset.random() }.joinToString("")

        val newRoom = RoomDoc(
            roomCode = roomCode,
            hostEmail = hostEmail,
            status = RoomStatus.WAITING.toString()
        )

        val success = roomRepository.createRoom(newRoom)

        if (!success) {
            throw Exception("Failed to create room in database")
        }

        return RoomResponse(
            roomCode = roomCode,
            hostEmail = hostEmail,
            guestEmail = null,
            status = RoomStatus.WAITING,
            message = "Room created successfully"
        )
    }

    suspend fun getRoom(roomCode: String): RoomResponse {
        val room = roomRepository.getRoomByCode(roomCode)
            ?: throw Exception("Room not found!")
        return RoomResponse(
            roomCode = room.roomCode,
            hostEmail = room.hostEmail,
            guestEmail = room.guestEmail,
            status = if (room.status == RoomStatus.PLAYING.toString()) RoomStatus.PLAYING else RoomStatus.WAITING,
            message = "OK"
        )
    }

    suspend fun joinRoom(guestEmail: String, roomCode: String): RoomResponse {
        val room = roomRepository.getRoomByCode(roomCode)
            ?: throw Exception("Room not found!")

        if (room.guestEmail != null || room.status == RoomStatus.PLAYING.toString()) {
            throw Exception("Room is already full or playing!")
        }

        if (room.hostEmail == guestEmail) {
            throw Exception("You cannot join your own room!")
        }

        val success = roomRepository.updateRoomGuest(roomCode, guestEmail)

        if (!success) {
            throw Exception("Failed to join room. Please try again.")
        }

        return RoomResponse(
            roomCode = roomCode,
            hostEmail = room.hostEmail,
            guestEmail = guestEmail,
            status = RoomStatus.PLAYING,
            message = "Joined successfully. Ready to play!"
        )
    }
}