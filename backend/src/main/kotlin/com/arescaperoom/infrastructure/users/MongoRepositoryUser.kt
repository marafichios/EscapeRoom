package com.arescaperoom.infrastructure.users

import com.arescaperoom.domain.users.IUserRepository
import com.mongodb.client.model.Filters
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoRepositoryUser(db: CoroutineDatabase) : IUserRepository {
    private val users = db.getCollection<UserDoc>("users")

    override suspend fun findById(id: String): UserDoc? {
        return try {
            users.findOneById(id) // mail
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun findByEmail(email: String): UserDoc? {
        return users.findOne(UserDoc::email eq email)
    }

    override suspend fun save(user: UserDoc): Boolean {
        return try {
            users.insertOne(user).wasAcknowledged()
        } catch (e: Exception) {
            println("DEBUG: Failed to insert user into MongoDB: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun findAll(): List<UserDoc> {
        return try {
            users.find().toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun update(id: String, userDoc: UserDoc): Boolean {
        return try {
            val result = users.replaceOneById(id, userDoc)
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun delete(id: String): Boolean {
        return try {
            val result = users.deleteOneById(id)
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun searchByName(query: String): List<UserDoc> {
        return try {
            val regex = Filters.regex("name", "(?i).*${Regex.escape(query)}.*")
            users.find(regex).limit(20).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}