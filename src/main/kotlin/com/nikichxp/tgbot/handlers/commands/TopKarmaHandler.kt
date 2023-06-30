package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.service.UserInfo
import com.nikichxp.tgbot.service.menu.CommandHandler
import com.nikichxp.tgbot.service.tgapi.TgOperations
import com.nikichxp.tgbot.util.getContextChatId
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
    override fun isCommandSupported(command: String): Boolean = command.lowercase() in listOf("/top", "/realtop")

    override fun processCommand(args: List<String>, update: Update): Boolean {
        if (args.isNotEmpty()) {
            tgOperations.sendMessage(
                update.getContextChatId()!!,
                "Additional args are not supported yet"
            )
            return true
        }
        // TODO do create a DAO for userInfo and karma already!
        val users = mongoTemplate.find<UserInfo>(Query.query(Criteria.where("rating").ne(0.0)))
        val ratingStr = users.sortedBy { -it.rating }.joinToString(separator = "\n") {
            "${it.username ?: ("id=" + it.id.toString())}: ${it.rating}"
        }
        update.run {
            tgOperations.sendMessage(
                update.getContextChatId()!!,
                "Top users are:\n$ratingStr"
            )
        }
        return true
    }
}