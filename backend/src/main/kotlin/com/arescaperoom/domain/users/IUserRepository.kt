package com.arescaperoom.domain.users

import com.arescaperoom.infrastructure.users.UserDoc

interface IUserRepository {
    suspend fun save(user: UserDoc): Boolean
    suspend fun findById(id: String): UserDoc?
    suspend fun findByEmail(email: String): UserDoc?
    suspend fun findAll(): List<UserDoc>
    suspend fun update(id: String, userDoc: UserDoc): Boolean
    suspend fun delete(id: String): Boolean
    suspend fun searchByName(query: String): List<UserDoc>
}