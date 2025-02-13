package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.BotSupportFeature
import com.nikichxp.tgbot.core.handlers.ChatCommandsHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerExecutor
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerScanner
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.handlers.commands.SingleCommandHandler
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.query.update
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs
import kotlin.reflect.KFunction

@SpringBootTest(classes = [ChatCommandsHandler::class, CommandHandlerExecutor::class])
class ChatCommandTest {

    @MockitoBean
    lateinit var commandHandlerScanner: CommandHandlerScanner

    @Autowired
    lateinit var chatCommandsHandler: ChatCommandsHandler

    @Autowired
    lateinit var commandHandlerExecutor: CommandHandlerExecutor


    companion object {
        val whatWeExpect = AtomicReference(listOf<String>())

        @JvmStatic
        fun provideCommands() = listOf(
            Arguments.of("/hello arg two three", listOf("arg", "two", "three")),
            Arguments.of("/hello one two", listOf("one", "two")),
            Arguments.of("/hello", listOf<String>()),
            Arguments.of("/hello 1 2 3 4 5 6 7 8 9 10", (1..10).map { it.toString() }),
            Arguments.of("/hello a   b", listOf("a", "b"))
        )
    }

    @Suppress("RedundantSuspendModifier")
    @ParameterizedTest
    @MethodSource("provideCommands")
    fun `test how commands are parsed`(command: String, expectedArgs: List<String>) {

        whatWeExpect.set(expectedArgs)
        val bot = TgBot.NIKICHBOT

        class TestHandler : CommandHandler {
            @HandleCommand("/hello")
            suspend fun hello(args: List<String>) {
                assertThat(args).isEqualTo(whatWeExpect.get())
            }

            override fun supportedBots(): Set<TgBot> = setOf(bot)
        }

        val update = mock<Update>(RETURNS_DEEP_STUBS)
        val singleCommandHandler = SingleCommandHandler(
            command = "/hello",
            handler = TestHandler(),
            function = TestHandler::hello
        )

        `when`(update.message!!.text).thenReturn(command)
        `when`(update.bot).thenReturn(bot)
        `when`(commandHandlerScanner.getHandlers()).thenReturn(setOf(singleCommandHandler))

        runBlocking {
            chatCommandsHandler.handleUpdate(update)
        }
    }

}