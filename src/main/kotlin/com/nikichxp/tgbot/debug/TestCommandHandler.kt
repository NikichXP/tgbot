package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgButton
import com.nikichxp.tgbot.core.service.tgapi.TgKeyboard
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.service.tgapi.TgSendMessage
import com.nikichxp.tgbot.core.util.getContextChatId
import org.springframework.stereotype.Component

@Component
class TestCommandHandler(
    private val tgOperations: TgOperations,
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
        val message = TgSendMessage.create {
            replyToCurrentMessage()
            text = "Keyboard removed"
            removeKeyboard()
        }

        tgOperations.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/testkey")
    suspend fun testKeyboard(args: List<String>, update: Update): Boolean {
        val message = TgSendMessage.create {
            replyToCurrentMessage()
            text = "Here is your keyboard"
            withKeyboard(
                listOf(
                    listOf(
                        "Button 1",
                        "Button 2"
                    ),
                    listOf(
                        "Button 3",
                        "Button 4"
                    )
                )
            )
        }

        tgOperations.sendMessage(message, update.bot)
        return true
    }
}