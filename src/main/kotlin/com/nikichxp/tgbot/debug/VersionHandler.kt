package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Component

@Component
@PropertySource("classpath:version.properties")
class VersionHandler(
    private val tgOperations: TgOperations,
) : CommandHandler {

    @Value("\${buildInfo.version}")
    lateinit var version: String
    @Value("\${buildInfo.date}")
    lateinit var date: String

    override fun supportedBots(tgBot: TgBot) = TgBot.entries.toSet()

    override fun isCommandSupported(command: String) = command in listOf("/version", "/v", "/buildinfo")

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        tgOperations.replyToCurrentMessage("in development")
        tgOperations.replyToCurrentMessage("version: $version; build date: [$date]")
        return true
    }
}

