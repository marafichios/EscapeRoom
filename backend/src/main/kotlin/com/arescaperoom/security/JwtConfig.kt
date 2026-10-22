package com.arescaperoom.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.Date

// token pt sesiune sa ne asiguram ca userul care face request-ul e cel care a facut login ci nu doar ca stie emailul altuia
object JwtConfig {
    private val secret: String = System.getenv("JWT_SECRET") ?: "blackwood-manor-dev-secret"

    const val issuer = "arescaperoom-backend"
    const val realm = "arescaperoom"
    const val authName = "auth-jwt"

    private const val validityMs = 12L * 60 * 60 * 1000 // 12 ore

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier = JWT.require(algorithm)
        .withIssuer(issuer)
        .build()

    //emitem odata la authenticate
    fun makeToken(email: String): String = JWT.create()
        .withIssuer(issuer)
        .withClaim("email", email)
        .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
        .sign(algorithm)
}

fun ApplicationCall.authEmail(): String? =
    principal<JWTPrincipal>()?.payload?.getClaim("email")?.asString()
