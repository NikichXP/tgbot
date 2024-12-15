package com.nikichxp.tgbot.debug

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.error.NotHandledSituationError
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.ChatCommandParser
import com.nikichxp.tgbot.core.util.getContextChatId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LogAllMessagesHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper,
    private val appConfig: AppConfig
) : UpdateHandler, CommandHandler {

    private val loggingToModeMap = mutableMapOf<Long, Boolean>()
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun supportedBots() = TgBot.entries.toSet()

    override fun botSupported(bot: TgBot) = true

    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(UpdateMarker.ALL)
    }

    override suspend fun handleUpdate(update: Update) {
        val chatId = update.getContextChatId()

        if (update.message?.text?.startsWith(LOG_PREFIX) == true) {
            return
        }
        if (loggingToModeMap.isNotEmpty()) {
            logger.info(objectMapper.writeValueAsString(update))
        }
        if (chatId != null) {
            loggingToModeMap[chatId]?.let {
                if (!it) {
                    tgOperations.sendMessage(chatId, LOG_PREFIX + objectMapper.writeValueAsString(update))
                }
            }
        }
        loggingToModeMap.entries.filter { it.value }.forEach {
            tgOperations.sendMessage(it.key, LOG_PREFIX + objectMapper.writeValueAsString(update))
        }
    }

    @HandleCommand("/logging")
    suspend fun configureLogging(args: List<String>, update: Update): Boolean {
        val chatId = update.getContextChatId() ?: throw NotHandledSituationError()

        suspend fun notify(text: String) = tgOperations.sendMessage(chatId, LOG_PREFIX + text)

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
                        if (chatId == appConfig.adminId) {
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