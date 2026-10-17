package com.nikichxp.tgbot.karmabot.service

import com.nikichxp.bot.common.entity.model.UserModel
import com.nikichxp.tgbot.karmabot.entity.UserInfo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class UserService(
    private val mongoTemplate: MongoTemplate
) {

    fun getUserInfo(user: UserModel): UserInfo = modifyUser(user) {}

    fun modifyUser(user: UserModel, action: (UserInfo) -> Unit): UserInfo {
        val userInfo = mongoTemplate.findById(user.id) ?: UserInfo(user.id)
        userInfo.user = user
        action(userInfo)
        return mongoTemplate.save(userInfo)
    }

    fun findRatedUsers(): List<UserInfo> = mongoTemplate.find(Query.query(Criteria.where("rating").ne(0.0)))

}
