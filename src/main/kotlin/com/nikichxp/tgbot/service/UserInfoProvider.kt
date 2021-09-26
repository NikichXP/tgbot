package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.User
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class UserInfoProvider(
    private val mongoTemplate: MongoTemplate
) {

    fun getUserInfo(user: User): UserInfo {
        return mongoTemplate.findById(user.id) ?: UserInfo(user).also { mongoTemplate.insert(user) }
    }

}

data class UserInfo(
    @Id val id: Long,
    var username: String?,
    var rating: Double = 0.0
) {

    constructor(user: User) : this(id = user.id, username = user.username, rating = 0.0)

}