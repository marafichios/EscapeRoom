package com.arescaperoom.infrastructure.users

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

import kotlinx.serialization.SerialName

@Serializable
data class UserDoc(
    @SerialName("_id") val email: String,
    val name: String,
    val profilePicture: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)