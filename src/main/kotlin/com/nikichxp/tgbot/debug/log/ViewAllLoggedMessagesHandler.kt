package com.nikichxp.tgbot.debug.log

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.error.NotHandledSituationError
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.ChatCommandParser
import com.nikichxp.tgbot.core.util.getContextChatId
import org.springframework.stereotype.Component

@Component
class ViewAllLoggedMessagesHandler(
    private val tgOperations: TgOperations,
    private val appConfig: AppConfig,
    private val loggingConfigBackend: LoggingConfigBackend
) : CommandHandler {

    override fun requiredFeatures() = setOf(Features.DEBUG)

    @HandleCommand("/logging")
    suspend fun configureLogging(args: List<String>, update: Update): Boolean {
        val chatId = update.getContextChatId() ?: throw NotHandledSituationError()

        suspend fun notify(text: String) = tgOperations.sendMessage(chatId, LogAllMessagesHandler.Companion.LOG_PREFIX + text)

        return ChatCommandParser.Companion.analyze(args) {
            path("status") {
                notify("Logging status is: " + loggingConfigBackend.shouldLog(chatId))
            }
            paths("this", "set") {
                path("on") {
                    loggingConfigBackend.setLogging(chatId, true, false)
                    notify("logging status is set to on")
                }
                path("off") {
                    loggingConfigBackend.setLogging(chatId, false)
                    notify("logging status is set to off")
                }
            }
            path("admin") {
                path("all") {
                    path("on") {
                        if (chatId == appConfig.adminId) {
                            loggingConfigBackend.setLogging(chatId, true, true)
                            notify("admin log on")
                        } else {
                            notify("no admin status found")
                        }
                    }
                    path("off") {
                        loggingConfigBackend.setLogging(chatId, false)
                        notify("logging status is set to off")
                    }
                }
            }
        }
    }
}