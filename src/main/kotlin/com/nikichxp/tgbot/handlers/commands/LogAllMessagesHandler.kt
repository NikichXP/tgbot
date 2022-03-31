package com.nikichxp.tgbot.handlers.commands

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.error.NotHandledSituationError
import com.nikichxp.tgbot.handlers.UpdateHandler
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.menu.CommandHandler
import com.nikichxp.tgbot.util.ChatCommandParser
import com.nikichxp.tgbot.util.getContextChatId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LogAllMessagesHandler(
    private val tgOperations: TgOperations,
    private val objectMapper: ObjectMapper,
    private val updateProvider: CurrentUpdateProvider
) : UpdateHandler, CommandHandler {

    @Value("\${ADMIN_USER:0}")
    private var adminUser: Long = 0

    private val loggingToModeMap = mutableMapOf<Long, Boolean>()
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(UpdateMarker.ALL)
    }

    override fun handleUpdate(update: Update) {
        val chatId = update.getContextChatId()

        if (loggingToModeMap.isNotEmpty()) {
            logger.info(objectMapper.writeValueAsString(update))
        }
        if (update.message?.text?.startsWith(prefix) == true) {
            return
        }
        if (chatId != null) {
            loggingToModeMap[chatId]?.let {
                if (!it) {
                    tgOperations.sendMessage(chatId, prefix + objectMapper.writeValueAsString(update))
                }
            }
        }
        loggingToModeMap.entries.filter { it.value }.forEach {
            tgOperations.sendMessage(it.key, prefix + objectMapper.writeValueAsString(update))
        }
    }

    override fun isCommandSupported(command: String): Boolean = command == "/logging"

    override fun processCommand(args: List<String>): Boolean {
        val chatId = updateProvider.update?.getContextChatId() ?: throw NotHandledSituationError()

        fun notify(text: String) = tgOperations.sendMessage(chatId, prefix + text)

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
        const val prefix = "[logger]: "
    }
}