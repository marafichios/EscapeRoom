package com.arescaperoom.services

import com.arescaperoom.domain.users.IUserRepository
import com.arescaperoom.infrastructure.users.UserDoc
import com.arescaperoom.models.User
import com.arescaperoom.models.UserRequest

class UserService(private val userRepository: IUserRepository) {

    suspend fun loginOrRegister(userRequest: UserRequest): User {
        println("DEBUG: Incerc login pentru email: ${userRequest.email}")

        val existingDoc = userRepository.findByEmail(userRequest.email)
        if (existingDoc != null) {
            println("DEBUG: Utilizator gasit în DB: ${existingDoc.name}")
            return existingDoc.toUser()
        }

        println("DEBUG: Utilizator nou. Salvare in DB pentru: ${userRequest.username}")
        val newDoc = UserDoc(
            email = userRequest.email,
            name = userRequest.username,
            profilePicture = userRequest.profilePicture
        )

        val success = userRepository.save(newDoc)
        println("DEBUG: Rezultat salvare: $success")

        return newDoc.toUser()
    }

    suspend fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)?.toUser()
    }

    suspend fun getAllUsers(): List<User> {
        return userRepository.findAll().map { it.toUser() }
    }

    suspend fun deleteUser(email: String): Boolean {
        return userRepository.delete(email)
    }

    suspend fun searchByName(query: String): List<User> {
        if (query.isBlank() || query.length < 2) return emptyList()
        return userRepository.searchByName(query).map { it.toUser() }
    }

    // dto transformare Doc in Model API
    private fun UserDoc.toUser(): User {
        return User(
            email = this.email,
            username = this.name,
            profilePicture = this.profilePicture,
            createdAt = this.createdAt
        )
    }
}
