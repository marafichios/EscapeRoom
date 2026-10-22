package com.arescaperoom.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.arescaperoom.models.UserRequest
import com.arescaperoom.services.UserService
import kotlinx.serialization.Serializable

// Definim ce date așteptăm de la Android
@Serializable
data class AuthRequest(val idToken: String)

@Serializable
data class AuthResponse(val message: String)

private val userService = UserService()

fun Route.authRoutes() {

    // AICI pui Web Client ID-ul tău din Google Cloud
    val webClientId = "649480512750-3jc090uloejv2bi3fhqltvbgn1qcvosv.apps.googleusercontent.com"

    val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(listOf(webClientId))
        .build()

    post("/authenticate") {
        val request = call.receive<AuthRequest>()

        try {
            val idToken = verifier.verify(request.idToken)
            if (idToken != null) {
                val payload = idToken.payload
                val email = payload.email
                val name = payload["name"] as String

                // Check if user already exists in MongoDB
                var user = userService.getUserByEmail(email)

                if (user == null) {
                    user = userService.createUser(
                        UserRequest(
                            username = name,
                            email = email,
                            password = "google-oauth"
                        )
                    )
                }

                call.respond(HttpStatusCode.OK, AuthResponse(
                    message = "Autentificare reușită pentru $name"
                ))
            } else {
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(message = "Token invalid"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            call.respond(HttpStatusCode.BadRequest, AuthResponse(message = "Eroare la verificarea token-ului: ${e.message}"))
        }
    }
}