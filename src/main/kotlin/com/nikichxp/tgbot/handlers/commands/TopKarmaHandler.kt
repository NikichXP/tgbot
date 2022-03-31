package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.UserInfo
import com.nikichxp.tgbot.service.menu.CommandHandler
import com.nikichxp.tgbot.util.getContextChatId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class TopKarmaHandler(
    private val tgOperations: TgOperations,
    private val updateProvider: CurrentUpdateProvider,
    private val mongoTemplate: MongoTemplate
) : CommandHandler {
    override fun isCommandSupported(command: String): Boolean = command.lowercase() in listOf("/top", "/realtop")

    override fun processCommand(args: List<String>): Boolean {
        if (args.isNotEmpty()) {
            tgOperations.sendMessage(
                updateProvider.update?.getContextChatId()!!,
                "Additional args are not supported yet"
            )
            return true
        }
        // TODO do create a DAO for userInfo and karma already!
        val users = mongoTemplate.find<UserInfo>(Query.query(Criteria.where("rating").ne(0.0)))
        val ratingStr = users.sortedBy { -it.rating }.joinToString(separator = "\n") {
            "${it.username ?: ("id=" + it.id.toString())}: ${it.rating}"
        }
        tgOperations.sendMessage(
            updateProvider.update?.getContextChatId()!!,
            "Top users are:\n$ratingStr"
        )
        return true
    }
}