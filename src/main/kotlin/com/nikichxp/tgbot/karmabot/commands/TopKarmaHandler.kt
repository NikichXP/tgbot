package com.nikichxp.tgbot.karmabot.commands

import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.karmabot.service.UserInfo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class TopKarmaHandler(
    private val tgMessageService: TgMessageService,
    private val mongoTemplate: MongoTemplate
) : CommandHandler {

    override fun requiredFeatures() = setOf(Features.KARMA)

    // TODO add realtop
    @HandleCommand("/top")
    suspend fun processCommand(): Boolean {

        // TODO do create a DAO for userInfo and karma already!
        val users = mongoTemplate.find<UserInfo>(Query.query(Criteria.where("rating").ne(0.0)))
        val ratingStr = users.sortedBy { -it.rating }.joinToString(separator = "\n") {
            "${it.username ?: ("id=" + it.id.toString())}: ${it.rating}"
        }

        tgMessageService.sendMessage {
            sendInCurrentChat()
            text = "Top users are:\n$ratingStr"
        }
        return true
    }
}