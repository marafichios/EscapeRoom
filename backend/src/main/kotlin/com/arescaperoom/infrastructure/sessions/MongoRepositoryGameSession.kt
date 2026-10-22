package com.arescaperoom.infrastructure.sessions

import com.arescaperoom.domain.sessions.IGameSessionRepository
import com.arescaperoom.models.LeaderboardEntryResponse
import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.litote.kmongo.ascending
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoRepositoryGameSession(db: CoroutineDatabase) : IGameSessionRepository {

    private val sessions = db.getCollection<GameSessionDoc>("game_sessions")

    init {
        runBlocking {
            // index compus pt cautare clasament
            // match(roomId) + sort(durationSeconds)
            // O(log n + k)
            sessions.ensureIndex(
                Indexes.ascending("roomId", "durationSeconds"),
                IndexOptions().background(true).name("leaderboard_idx")
            )
            // Index simplu pentru interogări după jucător
            sessions.ensureIndex(
                Indexes.ascending("playerId"),
                IndexOptions().background(true).name("player_idx")
            )
        }
    }

    override suspend fun saveSession(session: GameSessionDoc): Boolean {
        return try {
            sessions.insertOne(session).wasAcknowledged()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getSessionsByPlayer(playerId: String): List<GameSessionDoc> {
        return sessions
            .find(GameSessionDoc::playerId eq playerId)
            .toList()
    }

    override suspend fun getBestSessionByPlayer(playerId: String): GameSessionDoc? {
        return sessions
            .find(GameSessionDoc::playerId eq playerId)
            .sort(ascending(GameSessionDoc::durationSeconds))
            .first()
    }

    /**
     *
     * Etapa 1 — $match: filtrează sesiunile pentru capitolul cerut.
     *   Foloseşte compound index-ul (roomId, durationSeconds) → index range scan.
     *
     * Etapa 2 — $sort: sortează după durationSeconds crescător înainte de grupare.
     *   Deoarece avem index-ul compus, această sortare este "free" (index order).
     *   Critica: trebuie să sortăm ÎNAINTE de $group ca $first să returneze minimul.
     *
     * Etapa 3 — $addFields: calculează o cheie canonică pentru pereche.
     *   "Andra|Mihai" == "Mihai|Andra" → evităm numărarea aceluiaşi joc de două ori.
     *   Folosim $cond cu $lte pentru a alege ordinea alfabetică.
     *
     * Etapa 4 — $group: grupează după cheie canonică, păstrând primul rezultat
     *   (= cel mai rapid, datorită sortării din Etapa 2) pentru fiecare pereche.
     *
     * Etapa 5 — $sort: re-sortăm după grupare deoarece $group nu garantează ordine.
     *
     * Etapa 6 — $limit: returnăm doar primele [limit] intrări.
     */
    override suspend fun getLeaderboard(roomIds: List<String>, limit: Int, skip: Int): List<LeaderboardEntryResponse> {
        val pipeline = listOf(
            // 1. filtrare capitol
            Aggregates.match(
                com.mongodb.client.model.Filters.`in`("roomId", roomIds)
            ),

            // 2. sort asc inainte de grupare
            Aggregates.sort(
                com.mongodb.client.model.Sorts.ascending("durationSeconds")
            ),

            //3. folosesc email ca id, calculez perechea cu ord alfabetica
            Document("\$addFields", Document("pairId",
                Document("\$cond", listOf(
                    Document("\$and", listOf(
                        Document("\$ne", listOf("\$partnerId", null)),
                        Document("\$ne", listOf("\$partnerId", ""))
                    )),
                    "\$partnerId",
                    "\$partnerName"
                ))
            )),

            // 3.1 o sg intrare per echipa
            Document("\$addFields", Document("pairKey",
                Document("\$cond", listOf(
                    Document("\$lte", listOf("\$playerId", "\$pairId")),
                    Document("\$concat", listOf("\$playerId", "|", "\$pairId")),
                    Document("\$concat", listOf("\$pairId", "|", "\$playerId"))
                ))
            )),


            // 4 grupez dupa cheia de sus pt o sg intrare per echipa
            Document("\$group", Document("_id",
                    Document("matchId", "\$matchId")
                        .append("pair", "\$pairKey")
                        .append("dur", "\$durationSeconds"))
                .append("pairKey",         Document("\$first", "\$pairKey"))
                .append("durationSeconds", Document("\$first", "\$durationSeconds"))
                .append("playerId",        Document("\$first", "\$playerId"))
                .append("playerName",      Document("\$first", "\$playerName"))
                .append("partnerName",     Document("\$first", "\$partnerName"))
                .append("hintsUsed",       Document("\$sum",   "\$hintsUsed"))
                .append("voiceSeconds",    Document("\$sum",   "\$voiceSeconds"))
                .append("chatMessages",    Document("\$sum",   "\$chatMessages"))
                .append("completedAt",     Document("\$first", "\$completedAt"))
            ),

            // 5 sort asc dupa durata
            Aggregates.sort(
                com.mongodb.client.model.Sorts.ascending("durationSeconds")
            ),

            // 6 grupez pe echipa
            Document("\$group", Document("_id", "\$pairKey")
                .append("games", Document("\$push", "\$\$ROOT"))
            ),

            // 7 pastrez 5 jocuri max pe echipa
            Document("\$project", Document("games",
                Document("\$slice", listOf("\$games", 5))
            )),

            // 8. un rand per joc
            Document("\$unwind", "\$games"),
            Document("\$replaceRoot", Document("newRoot", "\$games")),

            // 9. sort global durata
            Aggregates.sort(
                com.mongodb.client.model.Sorts.ascending("durationSeconds")
            ),


            //9.1 scot id uri composed
            Document("\$project", Document("_id", 0)
                .append("playerId", 1)
                .append("playerName", 1)
                .append("partnerName", 1)
                .append("durationSeconds", 1)
                .append("hintsUsed", 1)
                .append("voiceSeconds", 1)
                .append("chatMessages", 1)
                .append("completedAt", 1)
            ),

            // 10 paginare
            Aggregates.skip(skip),
            Aggregates.limit(limit)
        )

        return sessions.aggregate<LeaderboardEntryResponse>(pipeline).toList()
    }
}
