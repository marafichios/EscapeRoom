package com.arescaperoom.routes

import com.arescaperoom.models.ApiResponse
import com.arescaperoom.models.ErrorResponse
import com.arescaperoom.models.UserRequest
import com.arescaperoom.security.authEmail
import com.arescaperoom.security.JwtConfig
import com.arescaperoom.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userService by inject<UserService>()

    route("/api/users") {
        authenticate(JwtConfig.authName) {

            // GET /api/users/search?name= caut dupa nume sa invit useri
            get("/search") {
                val name = call.request.queryParameters["name"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(error = "name parameter required"))
                val results = userService.searchByName(name)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = results))
            }

            get {
                val users = userService.getAllUsers()
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = users))
            }

            // .profil public cu nume poza
            get("/{email}") {
                val email = call.parameters["email"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Email missing")

                val user = userService.getUserByEmail(email)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = user))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "User not found"))
                }
            }

            delete("/{email}") {
                val email =
                    call.parameters["email"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Email missing")
                if (call.authEmail() != email) {
                    return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponse(error = "Nu poți șterge alt cont"))
                }

                val success = userService.deleteUser(email)
                if (success) {
                    call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "User deleted"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(error = "User not found"))
                }
            }
        }
    }
}