package com.arescaperoom.routes

import com.arescaperoom.models.ApiResponse
import com.arescaperoom.models.ErrorResponse
import com.arescaperoom.models.GameSessionRequest
import com.arescaperoom.security.authEmail
import com.arescaperoom.security.JwtConfig
import com.arescaperoom.services.GameSessionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.gameSessionRoutes() {
    val sessionService by inject<GameSessionService>()

    route("/api/sessions") {

        // POST /api/sessions Unity trimite rez la final
        post {
            val request = call.receive<GameSessionRequest>()
            val success = sessionService.saveSession(request)
            if (success) {
                call.respond(HttpStatusCode.Created, ApiResponse<Unit>(success = true, message = "Session saved"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = "Failed to save session"))
            }
        }

        authenticate(JwtConfig.authName) {

            // GET /api/sessions/leaderboard/{chapterId} top echipe sortate dupa timp
            route("/leaderboard") {

                // paginat cu infinite scroll
                get("/{chapterId}") {
                    val chapterId = call.parameters["chapterId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "chapterId missing"))
                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 50
                    val entries = sessionService.getLeaderboard(chapterId, page, size)
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = entries))
                }
            }

            // GET /api/sessions/{playerId} toate sesiunile unui user
            get("/{playerId}") {
                val playerId = call.parameters["playerId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "playerId missing"))
                if (call.authEmail() != playerId) {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți accesa sesiunile altui jucător"))
                }
                val sessions = sessionService.getSessionsByPlayer(playerId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = sessions))
            }

            // GET /api/sessions/best/{playerId} cel mai bun run al unui user
            get("/best/{playerId}") {
                val playerId = call.parameters["playerId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "playerId missing"))
                if (call.authEmail() != playerId) {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți accesa sesiunile altui jucător"))
                }
                val best = sessionService.getBestSession(playerId)
                if (best != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = best))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "No sessions found"))
                }
            }

            // GET /api/sessions/completed/{playerId}/{chapterId} daca a jucat capitolul sau nu
            get("/completed/{playerId}/{chapterId}") {
                val playerId  = call.parameters["playerId"]  ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "playerId missing"))
                val chapterId = call.parameters["chapterId"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "chapterId missing"))
                if (call.authEmail() != playerId) {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți accesa datele altui jucător"))
                }
                val completed = sessionService.hasCompletedChapter(playerId, chapterId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = mapOf("completed" to completed)))
            }
        }
    }
}
