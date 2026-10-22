package com.arescaperoom.routes

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        healthCheck()
        authRoutes()
        userRoutes()
    }
}
