package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.User
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class UserService(
    private val mongoTemplate: MongoTemplate
) {

    fun getUserInfo(user: User): UserInfo {
        return mongoTemplate.findById(user.id) ?: UserInfo(user).also { mongoTemplate.insert(user) }
    }

    fun modifyUser(id: Long, action: (UserInfo) -> Unit) {
        val userInfo: UserInfo = mongoTemplate.findById(id) ?: throw IllegalArgumentException("user not found")
        action(userInfo)
        mongoTemplate.save(userInfo)
    }

    fun modifyUser(user: User, action: (UserInfo) -> Unit) {
        val userInfo = mongoTemplate.findById(user.id) ?: UserInfo(user)
        action(userInfo)
        mongoTemplate.save(userInfo)
    }

}

data class UserInfo(
    @Id val id: Long,
    var username: String?,
    var rating: Double = 0.0
) {

    constructor(user: User) : this(id = user.id, username = user.username, rating = 0.0)

}