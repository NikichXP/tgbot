package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.config.AppConfig
import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.service.EmojiService
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.menu.CommandHandler
import com.nikichxp.tgbot.util.ChatCommandParser
import org.springframework.stereotype.Component

@Component
class RegisterEmojiHandler(
    private val updateProvider: CurrentUpdateProvider,
    private val tgOperations: TgOperations,
    private val emojiService: EmojiService,
    appConfig: AppConfig
) : CommandHandler {

    private var ownerId = appConfig.ownerId

    override fun isCommandSupported(command: String): Boolean = command == "/emoji"

    override fun processCommand(args: List<String>): Boolean {
        return ChatCommandParser.analyze(args) {
            path("set") {
                asArg("emoji") {
                    asArg("power") proceed@{
                        onEmojiSet(vars["emoji"], vars["power"])
                    }
                }
            }
            path("list") {
                val emojis = emojiService.listEmojis().joinToString(separator = "\n") { "${it.first} -> ${it.second}" }
                tgOperations.replyToCurrentMessage("Here are emoji list:\n$emojis")
            }
        }
    }

    fun onEmojiSet(emoji: String?, powerInput: String?) =
        when (val power = powerInput?.toDoubleOrNull()) {
            null -> tgOperations.replyToCurrentMessage("Cannot find the power of the emoji")
            !in -1.0..1.0 -> tgOperations.replyToCurrentMessage("Power can be in range from -1 to +1")
            else -> {
                val messageAuthorId = updateProvider.update?.message?.from?.id
                when {
                    messageAuthorId != ownerId -> tgOperations.replyToCurrentMessage("You can't do that")
                    emoji == null -> tgOperations.replyToCurrentMessage("Cannot find emoji info")
                    else -> {
                        val str = emoji.toCharArray().map {
                            val status = emojiService.saveEmojiInfo(emoji, power)
                            return@map "Add emoji $emoji status: $status; power = $power"
                        }.joinToString(separator = "\n")
                        tgOperations.replyToCurrentMessage(str)
                    }
                }
            }
        }
}
