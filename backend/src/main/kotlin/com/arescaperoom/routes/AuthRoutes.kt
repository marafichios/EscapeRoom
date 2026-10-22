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
import com.arescaperoom.security.JwtConfig
import com.arescaperoom.services.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class AuthRequest(val idToken: String)

@Serializable
data class AuthResponse(
    val message: String,
    val username: String? = null,
    val email: String? = null,
    val token: String? = null // JWT Bearer
)

fun Route.authRoutes() {
    val userService by inject<UserService>()

    // Google Cloud
    val webClientId = "649480512750-3jc090uloejv2bi3fhqltvbgn1qcvosv.apps.googleusercontent.com"

    val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
        .setAudience(listOf(webClientId))
        .build()

    post("/authenticate") {
        val request = call.receive<AuthRequest>()
        println("DEBUG: Reached /authenticate endpoint")
        
        try {
            println("DEBUG: Verifying token")
            val idToken = withContext(Dispatchers.IO) {
                verifier.verify(request.idToken)
            }
            if (idToken != null) {
                println("DEBUG: Token is valid")
                val payload = idToken.payload
                val email = payload.email
                val name = payload["name"] as String
                val picture = payload["picture"] as? String

                val user = userService.loginOrRegister(
                    UserRequest(
                        username = name,
                        email = email,
                        profilePicture = picture
                    )
                )

                val sessionToken = JwtConfig.makeToken(user.email)

                call.respond(HttpStatusCode.OK, AuthResponse(
                    message = "Autentificare reusita pentru ${user.username}",
                    username = user.username,
                    email = user.email,
                    token = sessionToken,
                ))
            } else {
                println("DEBUG: verifier.verify returned null. Token invalid or wrong audience.")
                call.respond(HttpStatusCode.Unauthorized, AuthResponse(message = "Token invalid"))
            }
        } catch (e: Exception) {
            println("DEBUG: Exception during token verification: ${e.message}")
            e.printStackTrace()
            call.respond(HttpStatusCode.BadRequest, AuthResponse(message = "Eroare: ${e.message}"))
        }
    }
}