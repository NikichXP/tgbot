package com.nikichxp.tgbot.handlers.commands

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.entity.UpdateContext
import com.nikichxp.tgbot.entity.UpdateContextHandler
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.error.NotHandledSituationError
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.service.tgapi.TgOperations
import com.nikichxp.tgbot.util.ChatCommandParser
import com.nikichxp.tgbot.util.getContextChatId
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LogAllMessagesHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper
) : UpdateHandler, CommandHandler {

    @Value("\${ADMIN_USER:0}")
    private var adminUser: Long = 0

    private val loggingToModeMap = mutableMapOf<Long, Boolean>()
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun supportedBots(tgBot: TgBot) = TgBot.entries.toSet()

    override fun botSupported(bot: TgBot) = true

    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(UpdateMarker.ALL)
    }

    override suspend fun handleUpdate(context: UpdateContext) {
        val chatId = context.update.getContextChatId()

        if (context.update.message?.text?.startsWith(LOG_PREFIX) == true) {
            return
        }
        if (loggingToModeMap.isNotEmpty()) {
            logger.info(objectMapper.writeValueAsString(context.update))
        }
        if (chatId != null) {
            loggingToModeMap[chatId]?.let {
                if (!it) {
                    tgOperations.sendMessage(chatId, LOG_PREFIX + objectMapper.writeValueAsString(context.update), context.tgBot)
                }
            }
        }
        loggingToModeMap.entries.filter { it.value }.forEach {
            tgOperations.sendMessage(it.key, LOG_PREFIX + objectMapper.writeValueAsString(context.update), context.tgBot)
        }
    }

    override fun isCommandSupported(command: String): Boolean = command == "/logging"

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        val chatId = update.getContextChatId() ?: throw NotHandledSituationError()

        suspend fun notify(text: String) = tgOperations.sendMessage(chatId, LOG_PREFIX + text, update.bot)

        return ChatCommandParser.analyze(args) {
            path("status") {
                notify("Logging status is: " + (loggingToModeMap[chatId] ?: false))
            }
            paths("this", "set") {
                path("on") {
                    loggingToModeMap[chatId] = false
                    notify("logging status is set to on")
                }
                path("off") {
                    loggingToModeMap.remove(chatId)
                    notify("logging status is set to off")
                }
            }
            path("admin") {
                path("all") {
                    path("on") {
                        if (chatId == adminUser) {
                            loggingToModeMap[chatId] = true
                            notify("admin log on")
                        } else {
                            notify("no admin status found")
                        }
                    }
                    path("off") {
                        loggingToModeMap.remove(chatId)
                        notify("logging status is set to off")
                    }
                }
            }
        }
    }

    companion object {
        const val LOG_PREFIX = "[logger]: "
    }
}