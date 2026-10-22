package com.arescaperoom.domain.users

import com.arescaperoom.infrastructure.repository.model.UserDoc

interface IUserRepository {
    suspend fun findByEmail(email: String): UserDoc?
    suspend fun save(user: UserDoc)
}