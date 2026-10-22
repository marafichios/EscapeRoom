package com.arescaperoom.domain.invites

import com.arescaperoom.infrastructure.invites.InviteDoc

interface IInviteRepository {
    suspend fun create(invite: InviteDoc): String          // return id
    suspend fun findPendingForPlayer(toPlayerId: String): List<InviteDoc>
    //invites trm de un jucator inca active ca sa vada user1 cand user2 accepta
    suspend fun findActiveFromPlayer(fromPlayerId: String): List<InviteDoc>
    suspend fun findById(id: String): InviteDoc?
    suspend fun updateStatus(id: String, status: String): Boolean
    // setez status si cod real
    suspend fun updateStatusAndRoomCode(id: String, status: String, roomCode: String): Boolean
}
