package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.common.CallbackModel
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.BotFeature
import com.nikichxp.tgbot.core.handlers.ChatCallbackHandler
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ChatCallbackHandlerTest {

    private val featureA = BotFeature("FEATURE_A", "Feature A")
    private val unsupportedFeature = BotFeature("FEATURE_UNSUPPORTED", "Unsupported feature")

    @Test
    fun `handler is called when feature is supported and callback matches`() {
        var called = false
        val handler = object : CallbackHandler {
            override fun requiredFeatures() = setOf(featureA)
            override fun isCallbackSupported(callbackContext: CallbackContext) = callbackContext.data == "match"
            override suspend fun handleCallback(callbackContext: CallbackContext): Boolean {
                called = true
                return true
            }
        }

        val botInfo = TgBotInfo("test-bot", setOf("FEATURE_A", "FEATURE_B"))

        val updateContext = mock<UpdateContext>()
        val callbackModel = CallbackModel(
            userId = 1L,
            data = "match",
            messageText = "test",
            buttonText = "btn",
            chatId = 10L,
            messageId = 100L
        )
        `when`(updateContext.callback).thenReturn(callbackModel)
        `when`(updateContext.getBotInfo()).thenReturn(botInfo)

        val chatCallbackHandler = ChatCallbackHandler(listOf(handler))

        runBlocking {
            chatCallbackHandler.handleUpdate(updateContext)
        }

        assertThat(called).isTrue()
    }

    @Test
    fun `handler is skipped when required feature is not supported`() {
        var called = false
        val handler = object : CallbackHandler {
            override fun requiredFeatures() = setOf(unsupportedFeature)
            override fun isCallbackSupported(callbackContext: CallbackContext) = true
            override suspend fun handleCallback(callbackContext: CallbackContext): Boolean {
                called = true
                return true
            }
        }

        val botInfo = TgBotInfo("test-bot", setOf("FEATURE_A"))

        val updateContext = mock<UpdateContext>()
        val callbackModel = CallbackModel(
            userId = 1L,
            data = "match",
            messageText = "test",
            buttonText = "btn",
            chatId = 10L,
            messageId = 100L
        )
        `when`(updateContext.callback).thenReturn(callbackModel)
        `when`(updateContext.getBotInfo()).thenReturn(botInfo)

        val chatCallbackHandler = ChatCallbackHandler(listOf(handler))

        runBlocking {
            chatCallbackHandler.handleUpdate(updateContext)
        }

        assertThat(called).isFalse()
    }

    @Test
    fun `handler is skipped when authentication fails`() {
        var called = false
        val handler = object : CallbackHandler, Authenticable {
            override fun requiredFeatures() = setOf(featureA)
            override suspend fun authenticate(context: UpdateContext): Boolean = false
            override fun isCallbackSupported(callbackContext: CallbackContext) = true
            override suspend fun handleCallback(callbackContext: CallbackContext): Boolean {
                called = true
                return true
            }
        }

        val botInfo = TgBotInfo("test-bot", setOf("FEATURE_A"))

        val updateContext = mock<UpdateContext>()
        val callbackModel = CallbackModel(
            userId = 1L,
            data = "match",
            messageText = "test",
            buttonText = "btn",
            chatId = 10L,
            messageId = 100L
        )
        `when`(updateContext.callback).thenReturn(callbackModel)
        `when`(updateContext.getBotInfo()).thenReturn(botInfo)

        val chatCallbackHandler = ChatCallbackHandler(listOf(handler))

        runBlocking {
            chatCallbackHandler.handleUpdate(updateContext)
        }

        assertThat(called).isFalse()
    }
}
