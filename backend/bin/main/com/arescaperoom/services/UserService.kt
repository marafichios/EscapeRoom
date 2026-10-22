package com.arescaperoom.services

import com.arescaperoom.database.DatabaseFactory
import com.arescaperoom.models.User
import com.arescaperoom.models.UserRequest
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class UserService {
    private val collection = DatabaseFactory.database.getCollection<User>("users")
    
    suspend fun createUser(userRequest: UserRequest): User? {
        // Check if user already exists
        val existingUser = collection.findOne(User::email eq userRequest.email)
        if (existingUser != null) {
            return null // User already exists
        }
        
        val user = User(
            username = userRequest.username,
            email = userRequest.email,
            password = userRequest.password // TODO: Hash password in production
        )
        
        collection.insertOne(user)
        return user
    }
    
    suspend fun getUserById(id: String): User? {
        return try {
            collection.findOneById(ObjectId(id))
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getUserByEmail(email: String): User? {
        return collection.findOne(User::email eq email)
    }
    
    suspend fun getAllUsers(): List<User> {
        return collection.find().toList()
    }
    
    suspend fun updateUser(id: String, userRequest: UserRequest): Boolean {
        return try {
            val result = collection.updateOneById(
                ObjectId(id),
                org.litote.kmongo.combine(
                    setValue(User::username, userRequest.username),
                    setValue(User::email, userRequest.email),
                    setValue(User::password, userRequest.password)
                )
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun deleteUser(id: String): Boolean {
        return try {
            val result = collection.deleteOneById(ObjectId(id))
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun authenticateUser(email: String, password: String): User? {
        val user = getUserByEmail(email)
        return if (user?.password == password) user else null
    }
}
