package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.ChatCommandsHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerExecutor
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerScanner
import com.nikichxp.tgbot.core.handlers.commands.SingleCommandHandler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(classes = [ChatCommandsHandler::class, CommandHandlerExecutor::class])
class ChatCommandTest {

    @MockitoBean
    lateinit var commandHandlerScanner: CommandHandlerScanner

    @Autowired
    lateinit var chatCommandsHandler: ChatCommandsHandler

    @Autowired
    lateinit var commandHandlerExecutor: CommandHandlerExecutor

    @Test
    fun `test how commands are parsed`() {
        val command = "/hello arg two three"
        val expectedArgs = listOf("arg", "two", "three")

        val update = mock<Update>(RETURNS_DEEP_STUBS)
        val commandHandler = mock<CommandHandler>()
        val singleCommandHandler = SingleCommandHandler(
            command = "/hello",
            function = mock(),
            handler = commandHandler
        )

        `when`(update.message!!.text).thenReturn(command)
        `when`(commandHandlerScanner.getHandlers()).thenReturn(setOf(singleCommandHandler))
        `when`(commandHandler.isBotSupported(any<TgBot>())).thenReturn(true)

        runBlocking {
            val result = chatCommandsHandler.handleUpdate(update)
            println()
        }

    }
}