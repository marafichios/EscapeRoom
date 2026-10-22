package com.arescaperoom.infrastructure.invites

import com.arescaperoom.domain.invites.IInviteRepository
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.`in`
import org.litote.kmongo.setValue
import java.util.concurrent.TimeUnit

class MongoRepositoryInvite(db: CoroutineDatabase) : IInviteRepository {

    private val invites = db.getCollection<InviteDoc>("invites")

    init {
        runBlocking {
            // ttl index
            invites.ensureIndex(
                Indexes.ascending("expiresAt"),
                IndexOptions().expireAfter(0, TimeUnit.SECONDS).name("invite_ttl_idx")
            )
            // Index compus
            invites.ensureIndex(
                Indexes.ascending("toPlayerId", "status"),
                IndexOptions().background(true).name("invite_receiver_idx")
            )
        }
    }

    override suspend fun create(invite: InviteDoc): String {
        invites.insertOne(invite)
        return invite.id.toString()
    }

    override suspend fun findPendingForPlayer(toPlayerId: String): List<InviteDoc> {
        return invites.find(
            and(
                InviteDoc::toPlayerId eq toPlayerId,
                InviteDoc::status eq InviteStatus.PENDING.name
            )
        ).toList()
    }

    override suspend fun findActiveFromPlayer(fromPlayerId: String): List<InviteDoc> {
        //invitatiile si starile lor sa afle user1 ce se intampla
        return invites.find(
            and(
                InviteDoc::fromPlayerId eq fromPlayerId,
                InviteDoc::status `in` listOf(
                    InviteStatus.PENDING.name,
                    InviteStatus.ACCEPTED.name,
                    InviteStatus.DECLINED.name,
                    InviteStatus.CANCELLED.name
                )
            )
        ).toList()
    }

    override suspend fun findById(id: String): InviteDoc? {
        return try {
            invites.findOneById(ObjectId(id))
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateStatus(id: String, status: String): Boolean {
        return try {
            val result = invites.updateOneById(
                ObjectId(id),
                setValue(InviteDoc::status, status)
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateStatusAndRoomCode(id: String, status: String, roomCode: String): Boolean {
        return try {
            val result = invites.updateOneById(
                ObjectId(id),
                com.mongodb.client.model.Updates.combine(
                    setValue(InviteDoc::status, status),
                    setValue(InviteDoc::roomCode, roomCode)
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}
