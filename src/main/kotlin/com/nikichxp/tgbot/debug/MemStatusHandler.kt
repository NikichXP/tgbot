package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.MemoryTrackerService
import org.springframework.stereotype.Component
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

@Component
class MemStatusHandler(
    private val memoryTrackerService: MemoryTrackerService,
    private val tgOperations: TgOperations,
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot): Set<TgBot> = setOf(TgBot.NIKICHBOT)

    @HandleCommand("/memstatus")
    suspend fun printMemoryStatus(): Boolean {
        tgOperations.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
        return true
    }
}

@Retention(AnnotationRetention.RUNTIME)
annotation class HandleCommand(val value: String)

@Component
class CommandHandlerScanner(
    private val commandHandlersV2: List<CommandHandler>,
) {

    fun getHandlers(): Set<SingleCommandHandler> {
        val result = mutableSetOf<SingleCommandHandler>()

        for (handler in commandHandlersV2) {
            handler::class.declaredFunctions
                .filter { it.hasAnnotation<HandleCommand>() }
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
}

class SingleCommandHandler(
    val command: String,
    val function: KFunction<*>,
    val handler: CommandHandler,
)

@Component
class CommandHandlerExecutor {

    suspend fun execute(handler: SingleCommandHandler, args: List<String>, update: Update): Boolean {
        val executionArgs = mutableListOf<Any>()

        for (parameter in handler.function.parameters) {
            if (parameter.type.isSubtypeOf(CommandHandler::class.createType())) {
                executionArgs.add(handler.handler)
            } else {
                when (parameter.type) {
                    List::class.createType() -> executionArgs.add(args)
                    Update::class.createType() -> executionArgs.add(update)
                    else -> throw IllegalStateException("Unknown parameter type: ${parameter.type}")
                }
            }
        }

        return handler.function.callSuspend(*executionArgs.toTypedArray()) as Boolean
    }

}