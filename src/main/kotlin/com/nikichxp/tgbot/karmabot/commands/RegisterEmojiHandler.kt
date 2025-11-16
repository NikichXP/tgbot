package com.nikichxp.tgbot.karmabot.commands

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.core.util.ChatCommandParser
import com.nikichxp.tgbot.karmabot.service.EmojiService
import org.springframework.stereotype.Component

@Component
class RegisterEmojiHandler(
    private val tgMessageService: TgMessageService,
    private val emojiService: EmojiService,
    appConfig: AppConfig,
) : CommandHandler {

    private var ownerId = appConfig.adminId

    override fun requiredFeatures() = setOf(Features.KARMA)

    @HandleCommand("/emoji")
    suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
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
                tgMessageService.replyToCurrentMessage("Here are emoji list:\n$emojis")
            }
        }
    }

    // TODO Refactor this
    suspend fun onEmojiSet(emoji: String?, powerInput: String?, update: Update) {
        when (val power = powerInput?.toDoubleOrNull()) {
            null -> tgMessageService.replyToCurrentMessage("Cannot find the power of the emoji")
            !in -1.0..1.0 -> tgMessageService.replyToCurrentMessage("Power can be in range from -1 to +1")
            else -> {
                val messageAuthorId = update.message?.from?.id
                when {
                    messageAuthorId != ownerId -> tgMessageService.replyToCurrentMessage("You can't do that")
                    emoji == null -> tgMessageService.replyToCurrentMessage("Cannot find emoji info")
                    else -> {
                        val str = emoji.split(",").map {
                            try {
                                val status = emojiService.saveEmojiInfo(it, power)
                                return@map "Add emoji $it status: $status; power = $power"
                            } catch (e: Exception) {
                                return@map "Add emoji $it status: fail; msg = ${e.message}"
                            }
                        }.joinToString(separator = "\n")
                        tgMessageService.replyToCurrentMessage(str)
                    }
                }
            }
        }
    }
}
