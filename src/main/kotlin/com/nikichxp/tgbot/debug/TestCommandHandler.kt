package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
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
) : CommandHandler, CallbackHandler {

    override fun supportedBots() = TgBot.entries.toSet()

    @HandleCommand("/ping")
    suspend fun processCommand(): Boolean {
        tgOperations.replyToCurrentMessage("pong!")
        return true
    }

    @HandleCommand("/myid")
    suspend fun myId(update: Update) {
        tgOperations.replyToCurrentMessage("Your id is ${update.getContextChatId()}")
    }

    @HandleCommand("/removekeys")
    suspend fun removeKeyboard(update: Update): Boolean {
        val message = TgSendMessage.create {
            replyToCurrentMessage()
            text = "Keyboard removed"
            removeKeyboard()
        }

        tgOperations.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/inlinekeys")
    suspend fun testInlineKeyboard(update: Update): Boolean {
        val message = TgSendMessage.create {
            replyToCurrentMessage()
            text = "Here is your inline keyboard"
            withInlineKeyboard(getKeys())
        }

        tgOperations.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/testkey")
    suspend fun testKeyboard(update: Update): Boolean {
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

    override fun isCallbackSupported(callbackContext: CallbackContext): Boolean {
        return callbackContext.data.startsWith("btn")
    }

    override suspend fun handleCallback(
        callbackContext: CallbackContext,
        update: Update
    ): Boolean {
        tgOperations.updateMessageText(
            chatId = callbackContext.chatId,
            messageId = callbackContext.messageId,
            text = callbackContext.buttonText,
            bot = callbackContext.bot,
            replyMarkup = getKeys()
        )
        return true
    }

    private fun getKeys() = listOf(
        listOf(
            "Button 1" to "btn-1",
            "Button 2" to "btn-2"
        ),
        listOf(
            "Button 3" to "btn-3",
            "Button 4" to "btn-4"
        )
    )
}