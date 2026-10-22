package com.arescaperoom.routes

import com.arescaperoom.models.HealthCheckResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthCheck() {
    get("/") {
        call.respond(
            HttpStatusCode.OK,
            HealthCheckResponse(
                status = "ok",
                service = "AR Escape Room Backend",
                version = "1.0.0"
            )
        )
    }
    
    get("/health") {
        call.respond(
            HttpStatusCode.OK,
            HealthCheckResponse(
                status = "healthy"
            )
        )
    }
}
