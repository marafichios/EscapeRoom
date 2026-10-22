package com.arescaperoom.domain.sessions

import com.arescaperoom.infrastructure.sessions.GameSessionDoc
import com.arescaperoom.models.LeaderboardEntryResponse

interface IGameSessionRepository {
    suspend fun saveSession(session: GameSessionDoc): Boolean
    suspend fun getSessionsByPlayer(playerId: String): List<GameSessionDoc>
    suspend fun getBestSessionByPlayer(playerId: String): GameSessionDoc?

    suspend fun getLeaderboard(roomIds: List<String>, limit: Int = 50, skip: Int = 0): List<LeaderboardEntryResponse>
}
