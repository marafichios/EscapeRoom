package com.arescaperoom.plugins

import com.arescaperoom.security.JwtConfig
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.application.*

fun Application.configureSecurity() {
    install(Authentication) {
        jwt(JwtConfig.authName) {
            realm = JwtConfig.realm
            verifier(JwtConfig.verifier)
            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                if (!email.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
