package com.nikichxp.tgbot.karmabot.commands

import com.nikichxp.bot.common.util.UserFormatter
import com.nikichxp.tgbot.karmabot.KarmaFeature
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import com.nikichxp.tgbot.karmabot.service.UserService
import org.springframework.stereotype.Component

@Component
class TopKarmaHandler(
    private val tgMessageService: ITgMessageService,
    private val userService: UserService
) : CommandHandler {

    override fun requiredFeatures() = setOf(KarmaFeature)

    // TODO add realtop
    @HandleCommand("/top")
    suspend fun processCommand(): Boolean {

        // TODO do create a DAO for userInfo and karma already!
        val ratingStr = userService.findRatedUsers().sortedByDescending { it.rating }.joinToString(separator = "\n") {
            "${it.user?.let(UserFormatter::displayName) ?: "id=${it.id}"}: ${it.rating}"
        }

        tgMessageService.sendMessage {
            sendInCurrentChat()
            text = "Top users are:\n$ratingStr"
        }
        return true
    }
}