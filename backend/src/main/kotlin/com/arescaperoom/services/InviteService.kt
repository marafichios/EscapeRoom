package com.arescaperoom.services

import com.arescaperoom.domain.invites.IInviteRepository
import com.arescaperoom.infrastructure.invites.InviteDoc
import com.arescaperoom.infrastructure.invites.InviteStatus
import com.arescaperoom.models.AcceptInviteResponse
import com.arescaperoom.models.InviteResponse
import com.arescaperoom.models.SendInviteRequest

class InviteService(private val repository: IInviteRepository) {

    suspend fun sendInvite(request: SendInviteRequest): String {
        val doc = InviteDoc(
            fromPlayerId   = request.fromPlayerId,
            fromPlayerName = request.fromPlayerName,
            toPlayerId     = request.toPlayerId,
            toPlayerName   = request.toPlayerName,
            roomCode       = request.roomCode
        )
        return repository.create(doc)
    }

    suspend fun getPendingInvites(toPlayerId: String): List<InviteResponse> =
        repository.findPendingForPlayer(toPlayerId).map { it.toResponse() }


    //invitatii trm de un jucator cele active
    suspend fun getSentInvites(fromPlayerId: String): List<InviteResponse> =
        repository.findActiveFromPlayer(fromPlayerId).map { it.toResponse() }

    suspend fun getInvite(id: String): InviteResponse? =
        repository.findById(id)?.toResponse()

    // user2 accepta si creaza camera
    suspend fun acceptInvite(id: String, roomCode: String): AcceptInviteResponse? {
        val invite = repository.findById(id) ?: return null
        if (invite.status != InviteStatus.PENDING.name) return null
        repository.updateStatusAndRoomCode(id, InviteStatus.ACCEPTED.name, roomCode)
        return AcceptInviteResponse(
            roomCode       = roomCode,
            fromPlayerName = invite.fromPlayerName
        )
    }

    suspend fun declineInvite(id: String): Boolean {
        val invite = repository.findById(id) ?: return false
        if (invite.status != InviteStatus.PENDING.name) return false
        return repository.updateStatus(id, InviteStatus.DECLINED.name)
    }

    //user1 renunta dupa ce user2 accepta
    suspend fun cancelInvite(id: String): Boolean {
        val invite = repository.findById(id) ?: return false
        if (invite.status != InviteStatus.ACCEPTED.name && invite.status != InviteStatus.PENDING.name)
            return false
        return repository.updateStatus(id, InviteStatus.CANCELLED.name)
    }

    // user1 se alatura
    suspend fun markJoined(id: String): Boolean {
        val invite = repository.findById(id) ?: return false
        if (invite.status != InviteStatus.ACCEPTED.name) return false
        return repository.updateStatus(id, InviteStatus.JOINED.name)
    }
}

private fun InviteDoc.toResponse() = InviteResponse(
    id             = id.toString(),
    fromPlayerId   = fromPlayerId,
    fromPlayerName = fromPlayerName,
    toPlayerId     = toPlayerId,
    toPlayerName   = toPlayerName,
    roomCode       = roomCode,
    status         = status,
    expiresAt      = expiresAt.time,
    createdAt      = createdAt.time
)
