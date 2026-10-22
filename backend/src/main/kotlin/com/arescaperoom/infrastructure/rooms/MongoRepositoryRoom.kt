package com.arescaperoom.infrastructure.rooms

import com.arescaperoom.domain.rooms.IRoomRepository
import com.arescaperoom.models.RoomStatus
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.litote.kmongo.combine

class MongoRepositoryRoom(db: CoroutineDatabase) : IRoomRepository {

    private val rooms = db.getCollection<RoomDoc>("rooms")

    override suspend fun createRoom(room: RoomDoc): Boolean {
        return try {
            rooms.insertOne(room).wasAcknowledged()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getRoomByCode(roomCode: String): RoomDoc? {
        return rooms.findOne(RoomDoc::roomCode eq roomCode)
    }

    override suspend fun updateRoomGuest(roomCode: String, guestEmail: String): Boolean {
        return try {
            val result = rooms.updateOne(
                RoomDoc::roomCode eq roomCode,
                combine(
                    setValue(RoomDoc::guestEmail, guestEmail),
                    setValue(RoomDoc::status, RoomStatus.PLAYING.toString())
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}