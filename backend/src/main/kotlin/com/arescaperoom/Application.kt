package com.arescaperoom

import com.arescaperoom.database.DatabaseFactory
import com.arescaperoom.plugins.configureKoin
import com.arescaperoom.plugins.configureSecurity
import com.arescaperoom.routes.configureRouting
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(
        Netty,
        port = System.getenv("SERVER_PORT")?.toInt() ?: 8081,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // init db
    DatabaseFactory.init()
    
    // plugins
    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureSecurity()

    // rute
    configureKoin()
    configureRouting()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Unknown error"))
            )
        }
    }
}
