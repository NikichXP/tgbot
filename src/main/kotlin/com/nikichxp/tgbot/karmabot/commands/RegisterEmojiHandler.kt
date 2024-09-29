package com.nikichxp.tgbot.karmabot.commands

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.ChatCommandParser
import com.nikichxp.tgbot.karmabot.service.EmojiService
import org.springframework.stereotype.Component

@Component
class RegisterEmojiHandler(
    private val tgOperations: TgOperations,
    private val emojiService: EmojiService,
    appConfig: AppConfig
) : CommandHandler {

    private var ownerId = appConfig.adminId

    override fun supportedBots(tgBot: TgBot) = setOf(TgBot.NIKICHBOT)

    override fun isCommandSupported(command: String): Boolean = command == "/emoji"

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        return ChatCommandParser.analyze(args) {
            path("set") {
                asArg("emoji") {
                    asArg("power") {
                        onEmojiSet(vars["emoji"], vars["power"], update)
                    }
                }
            }
            path("list") {
                val emojis = emojiService.listEmojis().joinToString(separator = "\n") { "${it.first} -> ${it.second}" }
                tgOperations.replyToCurrentMessage("Here are emoji list:\n$emojis")
            }
        }
    }

    // TODO Refactor this
    suspend fun onEmojiSet(emoji: String?, powerInput: String?, update: Update) {
        when (val power = powerInput?.toDoubleOrNull()) {
            null -> tgOperations.replyToCurrentMessage("Cannot find the power of the emoji")
            !in -1.0..1.0 -> tgOperations.replyToCurrentMessage("Power can be in range from -1 to +1")
            else -> {
                val messageAuthorId = update.message?.from?.id
                when {
                    messageAuthorId != ownerId -> tgOperations.replyToCurrentMessage("You can't do that")
                    emoji == null -> tgOperations.replyToCurrentMessage("Cannot find emoji info")
                    else -> {
                        val str = emoji.split(",").map {
                            try {
                                val status = emojiService.saveEmojiInfo(it, power)
                                return@map "Add emoji $it status: $status; power = $power"
                            } catch (e: Exception) {
                                return@map "Add emoji $it status: fail; msg = ${e.message}"
                            }
                        }.joinToString(separator = "\n")
                        tgOperations.replyToCurrentMessage(str)
                    }
                }
            }
        }
    }
}
