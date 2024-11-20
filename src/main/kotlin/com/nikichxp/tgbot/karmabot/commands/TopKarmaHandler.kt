package com.nikichxp.tgbot.karmabot.commands

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.karmabot.service.UserInfo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class TopKarmaHandler(
    private val tgOperations: TgOperations,
    private val mongoTemplate: MongoTemplate
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot) = setOf(TgBot.NIKICHBOT)

    // TODO add realtop
    @HandleCommand("/top")
    suspend fun processCommand(args: List<String>, update: Update): Boolean {
        if (args.isNotEmpty()) {
            tgOperations.sendToCurrentChat("Additional args are not supported yet")
            return true
        }
        // TODO do create a DAO for userInfo and karma already!
        val users = mongoTemplate.find<UserInfo>(Query.query(Criteria.where("rating").ne(0.0)))
        val ratingStr = users.sortedBy { -it.rating }.joinToString(separator = "\n") {
            "${it.username ?: ("id=" + it.id.toString())}: ${it.rating}"
        }
        update.run {
            tgOperations.sendToCurrentChat("Top users are:\n$ratingStr")
        }
        return true
    }
}