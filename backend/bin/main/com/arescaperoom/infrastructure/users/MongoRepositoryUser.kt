package com.arescaperoom.infrastructure.users

import com.arescaperoom.domain.users.IUserRepository
import com.arescaperoom.infrastructure.repository.model.UserDoc
import org.litote.kmongo.coroutine.CoroutineDatabase

class MongoRepositoryUser(db: CoroutineDatabase) : IUserRepository {
    private val users = db.getCollection<UserDoc>("users")

    override suspend fun findByEmail(email: String): UserDoc? {
        return users.findOne("{email: '$email'}")
    }

    override suspend fun save(user: UserDoc) {
        users.insertOne(user)
    }
}