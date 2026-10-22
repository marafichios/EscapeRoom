package com.arescaperoom.database

import com.mongodb.ConnectionString
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object DatabaseFactory {
    private val mongoHost = System.getenv("MONGODB_HOST") ?: "localhost"
    private val mongoPort = System.getenv("MONGODB_PORT") ?: "27017"
    private val mongoDatabase = System.getenv("MONGODB_DATABASE") ?: "ar_escape_room"
    private val mongoUsername = System.getenv("MONGODB_USERNAME") ?: "admin"
    private val mongoPassword = System.getenv("MONGODB_PASSWORD") ?: "admin123"
    
    val client = KMongo.createClient(
        ConnectionString("mongodb://$mongoUsername:$mongoPassword@$mongoHost:$mongoPort/$mongoDatabase?authSource=admin")
    ).coroutine
    
    val database = client.getDatabase(mongoDatabase)
    
    fun init() {
        println("Initializing database connection...")
        println("Connected to MongoDB: $mongoHost:$mongoPort/$mongoDatabase")
    }
    
    fun close() {
        client.close()
    }
}
