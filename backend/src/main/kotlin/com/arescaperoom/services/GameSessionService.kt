package com.arescaperoom.services

import com.arescaperoom.domain.sessions.IGameSessionRepository
import com.arescaperoom.infrastructure.sessions.GameSessionDoc
import com.arescaperoom.models.GameSessionRequest
import com.arescaperoom.models.GameSessionResponse
import com.arescaperoom.models.LeaderboardEntryResponse

class GameSessionService(private val repository: IGameSessionRepository) {

    suspend fun saveSession(request: GameSessionRequest): Boolean {
        val doc = GameSessionDoc(
            playerId        = request.playerId,
            playerName      = request.playerName,
            partnerName     = request.partnerName,
            partnerId       = request.partnerId,
            matchId         = request.matchId,
            roomId          = request.roomId,
            durationSeconds = request.durationSeconds,
            hintsUsed       = request.hintsUsed,
            chatMessages    = request.chatMessages,
            voiceSeconds    = request.voiceSeconds,
            completedAt     = request.completedAt
        )
        return repository.saveSession(doc)
    }

    suspend fun getSessionsByPlayer(playerId: String): List<GameSessionResponse> =
        repository.getSessionsByPlayer(playerId).map { it.toResponse() }

    suspend fun getBestSession(playerId: String): GameSessionResponse? =
        repository.getBestSessionByPlayer(playerId)?.toResponse()

    // Capitol → camerele lui. Chapter I are 2 roluri/camere: Elara + Julian.
    private fun chapterRooms(chapterId: String): List<String> = when (chapterId.lowercase()) {
        "elara", "julian", "chapter1", "1", "i" -> listOf("elara", "julian")
        else -> listOf(chapterId)
    }

    // Paginat: [page] (0-based) cu [size] intrări per pagină.
    suspend fun getLeaderboard(chapterId: String, page: Int = 0, size: Int = 50): List<LeaderboardEntryResponse> {
        val safeSize = size.coerceIn(1, 100)
        val safePage = page.coerceAtLeast(0)
        return repository.getLeaderboard(chapterRooms(chapterId), limit = safeSize, skip = safePage * safeSize)
    }

    suspend fun hasCompletedChapter(playerId: String, chapterId: String): Boolean =
        repository.getSessionsByPlayer(playerId).any { it.roomId == chapterId }
}

private fun GameSessionDoc.toResponse() = GameSessionResponse(
    id              = id.toString(),
    playerId        = playerId,
    partnerName     = partnerName,
    roomId          = roomId,
    durationSeconds = durationSeconds,
    hintsUsed       = hintsUsed,
    chatMessages    = chatMessages,
    voiceSeconds    = voiceSeconds,
    completedAt     = completedAt
)
