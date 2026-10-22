package com.arescaperoom.routes

import com.arescaperoom.models.CreateRoomRequest
import com.arescaperoom.models.ErrorResponse
import com.arescaperoom.models.JoinRoomRequest
import com.arescaperoom.security.authEmail
import com.arescaperoom.security.JwtConfig
import com.arescaperoom.services.RoomService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.roomRoutes() {
    val roomService by inject<RoomService>()

    route("/api/rooms") {
        authenticate(JwtConfig.authName) {

            post("/create") {
                try {
                    val request = call.receive<CreateRoomRequest>()

                    if (request.hostEmail.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Email-ul nu poate fi gol"))
                        return@post
                    }
                    if (call.authEmail() != request.hostEmail) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți crea o cameră în numele altui cont"))
                        return@post
                    }

                    val response = roomService.createRoom(request.hostEmail)
                    call.respond(HttpStatusCode.Created, response)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(error = e.message ?: "Error creating room"))
                }
            }

            // status camera pt polling deci pt ambii useri din echipa
            get("/{code}") {
                try {
                    val code = call.parameters["code"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(error = "Room code missing")
                    )
                    val response = roomService.getRoom(code)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = e.message ?: "Room not found"))
                }
            }

            post("/join") {
                try {
                    val request = call.receive<JoinRoomRequest>()

                    if (request.guestEmail.isBlank() || request.roomCode.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "Date incomplete"))
                        return@post
                    }
                    if (call.authEmail() != request.guestEmail) {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți intra în cameră în numele altui cont"))
                        return@post
                    }

                    val response = roomService.joinRoom(request.guestEmail, request.roomCode)
                    call.respond(HttpStatusCode.OK, response)

                } catch (e: Exception) {
                    // erori reguli business
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = e.message ?: "Error joining room"))
                }
            }
        }
    }
}