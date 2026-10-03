package com.nikichxp.tgbot.core.handlers.commands

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Component
class CommandHandlerScanner(
    private val commandHandlersV2: List<CommandHandler>,
) {

    fun getHandlers(): Set<SingleCommandHandler> {
        val result = mutableSetOf<SingleCommandHandler>()

        for (handler in commandHandlersV2) {
            handler::class.declaredFunctions
                .filter { it.hasAnnotation<HandleCommand>() }
                .filter { handler ->
                    handler.findAnnotation<HandleCommand>()?.value?.let { textIsCommand(it) } ?: false
                }
                .map {
                    SingleCommandHandler(
                        command = it.findAnnotation<HandleCommand>()!!.value,
                        function = it,
                        handler = handler
                    )
                }.forEach(result::add)
        }

        return result
    }

    private fun textIsCommand(text: String): Boolean {
        return text.startsWith("/").also { if (!it) logger.warn("Text is not a command: $text") }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }
}