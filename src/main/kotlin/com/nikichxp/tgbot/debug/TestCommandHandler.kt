package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgInlineKeyboard
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.core.service.tgapi.TgSendMessage
import com.nikichxp.tgbot.core.util.getContextChatId
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component
class TestCommandHandler(
    private val tgMessageService: TgMessageService,
) : CommandHandler, CallbackHandler {

    private val startTime = LocalDateTime.now()

    @HandleCommand("/uptime")
    suspend fun uptime() {
        val duration = Duration.between(startTime, LocalDateTime.now())
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        
        val formattedUptime = buildString {
            if (days > 0) append("${days}d")
            if (hours > 0 || days > 0) append("${hours}h")
            if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m")
            append("${seconds}s")
        }
        
        tgMessageService.replyToCurrentMessage("Uptime: $formattedUptime")
    }

    override fun requiredFeatures() = setOf(Features.DEBUG)

    @HandleCommand("/ping")
    suspend fun processCommand(): Boolean {
        tgMessageService.replyToCurrentMessage("pong!")
        return true
    }

    @HandleCommand("/myid")
    suspend fun myId(update: Update) {
        tgMessageService.replyToCurrentMessage("Your id is ${update.getContextChatId()}")
    }

    @HandleCommand("/removekeys")
    suspend fun removeKeyboard(update: Update): Boolean {
        val message = TgSendMessage.create {
            replyToCurrentMessage()
            text = "Keyboard removed"
            removeKeyboard()
        }

        tgMessageService.sendMessage(message, update.bot)
        return true
    }

    @HandleCommand("/inlinekeys")
    suspend fun testInlineKeyboard(update: Update): Boolean {
        val message = TgSendMessage.create {
            replyToCurrentMessage()
            text = "Here is your inline keyboard"
            withInlineKeyboard(getKeys())
        }

        tgMessageService.sendMessage(message, update.bot)
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

        tgMessageService.sendMessage(message, update.bot)
        return true
    }

    override fun isCallbackSupported(callbackContext: CallbackContext): Boolean {
        return callbackContext.data.startsWith("btn")
    }

    override suspend fun handleCallback(
        callbackContext: CallbackContext,
        update: Update,
    ): Boolean {
        tgMessageService.updateMessageText(
            chatId = callbackContext.chatId,
            messageId = callbackContext.messageId,
            text = callbackContext.buttonText,
            bot = callbackContext.botInfo,
            replyMarkup = TgInlineKeyboard.of(getKeys())
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