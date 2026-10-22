package com.arescaperoom.routes

import com.arescaperoom.models.AcceptInviteRequest
import com.arescaperoom.models.ApiResponse
import com.arescaperoom.models.ErrorResponse
import com.arescaperoom.models.SendInviteRequest
import com.arescaperoom.security.authEmail
import com.arescaperoom.security.JwtConfig
import com.arescaperoom.services.InviteService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.inviteRoutes() {
    val inviteService by inject<InviteService>()

    route("/api/invites") {
        authenticate(JwtConfig.authName) {

            // POST /api/invites trimite invite cu TTL 10 min
            post {
                val request = call.receive<SendInviteRequest>()
                if (call.authEmail() != request.fromPlayerId) {
                    return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți trimite invitații în numele altui cont"))
                }
                val id = inviteService.sendInvite(request)
                call.respond(HttpStatusCode.Created, ApiResponse(success = true, data = mapOf("inviteId" to id)))
            }

            // GET /api/invites/pending/{playerId} invitatii primite neexp
            get("/pending/{playerId}") {
                val playerId = call.parameters["playerId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "playerId missing"))
                if (call.authEmail() != playerId) {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți accesa invitațiile altui jucător"))
                }
                val invites = inviteService.getPendingInvites(playerId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = invites))
            }

            // GET /api/invites/sent/{playerId} invitatii trimise de user, active PENDING/ACCEPTED
            // user 1 polling
            get("/sent/{playerId}") {
                val playerId = call.parameters["playerId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "playerId missing"))
                if (call.authEmail() != playerId) {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți accesa invitațiile altui jucător"))
                }
                val invites = inviteService.getSentInvites(playerId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = invites))
            }

            // GET /api/invites/{id} invitatie dupa id
            // polling pt verificare anulare
            get("/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "id missing"))
                val invite = inviteService.getInvite(id)
                if (invite == null) {
                    return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found"))
                }
                val me = call.authEmail()
                if (me != invite.fromPlayerId && me != invite.toPlayerId) {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu ești parte din această invitație"))
                }
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = invite))
            }

            // PUT /api/invites/{id}/accept user 2 accept. trimit cod camera
            put("/{id}/accept") {
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "id missing"))
                val invite = inviteService.getInvite(id)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                if (call.authEmail() != invite.toPlayerId) {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Only destination user can accpet"))
                }
                val body = call.receive<AcceptInviteRequest>()
                val result = inviteService.acceptInvite(id, body.roomCode)
                if (result != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = result))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                }
            }

            // PUT /api/invites/{id}/decline destinatar refuza
            put("/{id}/decline") {
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "id missing"))
                val invite = inviteService.getInvite(id)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                if (call.authEmail() != invite.toPlayerId) {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Doar destinatarul poate refuza invitația"))
                }
                val success = inviteService.declineInvite(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Invite declined"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                }
            }

            // PUT /api/invites/{id}/cancel orice user da cancel dupa aacept
            put("/{id}/cancel") {
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "id missing"))
                val invite = inviteService.getInvite(id)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                val me = call.authEmail()
                if (me != invite.fromPlayerId && me != invite.toPlayerId) {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu ești parte din această invitație"))
                }
                val success = inviteService.cancelInvite(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Invite cancelled"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                }
            }

            // PUT /api/invites/{id}/joined ambii vor sa joace
            put("/{id}/joined") {
                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "id missing"))
                val invite = inviteService.getInvite(id)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                if (call.authEmail() != invite.fromPlayerId) {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Doar invitatorul poate marca acest status"))
                }
                val success = inviteService.markJoined(id)
                if (success) {
                    call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Joined"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "Invite not found or already processed"))
                }
            }
        }
    }
}
