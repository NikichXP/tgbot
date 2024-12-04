package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.*
import com.nikichxp.tgbot.core.util.getContextChatId
import org.springframework.stereotype.Component

@Component
class TestCommandHandler(
    private val tgOperations: TgOperations
) : CommandHandler {

    override fun supportedBots() = TgBot.entries.toSet()

    @HandleCommand("/ping")
    suspend fun processCommand(args: List<String>, update: Update): Boolean {
        tgOperations.replyToCurrentMessage("pong!")
        return true
    }

    @HandleCommand("/myid")
    suspend fun myId(update: Update) {
        tgOperations.replyToCurrentMessage("Your id is ${update.getContextChatId()}")
    }

    @HandleCommand("/removekeys")
    suspend fun removeKeyboard(args: List<String>, update: Update): Boolean {
        val message = TgSendMessage(
            chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id"),
            text = "Keyboard removed",
            replyMarkup = TgRemoveKeyboard()
        )

        tgOperations.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/testkey")
    suspend fun testKeyboard(args: List<String>, update: Update): Boolean {
        val keyboard = TgKeyboard(
            listOf(
                listOf(
                    TgButton("Button 1"),
                    TgButton("Button 2")
                ),
                listOf(
                    TgButton("Button 3"),
                    TgButton("Button 4")
                )
            )
        )

        val message = TgSendMessage(
            chatId = update.getContextChatId() ?: throw IllegalArgumentException("Can't get chat id"),
            text = "Here is your keyboard",
            replyMarkup = keyboard
        )

        tgOperations.sendMessage(message, update.bot)
        return true
    }
}