package com.arescaperoom.infrastructure.repository.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class UserDoc(
    @BsonId val email: String, // Folosim email-ul ca ID unic de la Google
    val name: String,
    val profilePicture: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)