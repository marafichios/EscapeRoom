package com.arescaperoom.routes

import com.arescaperoom.models.ApiResponse
import com.arescaperoom.models.ErrorResponse
import com.arescaperoom.models.UserRequest
import com.arescaperoom.models.toResponse
import com.arescaperoom.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val userService = UserService()

fun Route.userRoutes() {
    route("/api/users") {
        
        // Create a new user
        post {
            try {
                val userRequest = call.receive<UserRequest>()
                
                // Basic validation
                if (userRequest.username.isBlank() || userRequest.email.isBlank() || userRequest.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "All fields are required")
                    )
                    return@post
                }
                
                val user = userService.createUser(userRequest)
                
                if (user != null) {
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            data = user.toResponse(),
                            message = "User created successfully"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse(error = "User with this email already exists")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = "Invalid request", details = e.message)
                )
            }
        }
        
        // Get all users
        get {
            val users = userService.getAllUsers()
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = users.map { it.toResponse() }
                )
            )
        }
        
        // Get user by ID
        get("/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = "User ID is required")
                )
                return@get
            }
            
            val user = userService.getUserById(id)
            if (user != null) {
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        data = user.toResponse()
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(error = "User not found")
                )
            }
        }
        
        // Update user
        put("/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = "User ID is required")
                )
                return@put
            }
            
            try {
                val userRequest = call.receive<UserRequest>()
                val success = userService.updateUser(id, userRequest)
                
                if (success) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse<Unit>(
                            success = true,
                            message = "User updated successfully"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(error = "User not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = "Invalid request", details = e.message)
                )
            }
        }
        
        // Delete user
        delete("/{id}") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = "User ID is required")
                )
                return@delete
            }
            
            val success = userService.deleteUser(id)
            if (success) {
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse<Unit>(
                        success = true,
                        message = "User deleted successfully"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(error = "User not found")
                )
            }
        }
        
        // Authenticate user (login)
        post("/login") {
            try {
                val credentials = call.receive<Map<String, String>>()
                val email = credentials["email"]
                val password = credentials["password"]
                
                if (email.isNullOrBlank() || password.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(error = "Email and password are required")
                    )
                    return@post
                }
                
                val user = userService.authenticateUser(email, password)
                if (user != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = user.toResponse(),
                            message = "Login successful"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse(error = "Invalid credentials")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(error = "Invalid request", details = e.message)
                )
            }
        }
    }
}
