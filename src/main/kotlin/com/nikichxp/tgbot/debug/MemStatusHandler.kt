package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandlerV2
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.MemoryTrackerService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

@Component
class MemStatusHandler(
    private val memoryTrackerService: MemoryTrackerService,
    private val tgOperations: TgOperations,
) : CommandHandlerV2() {

    override fun supportedBots(tgBot: TgBot): Set<TgBot> = setOf(TgBot.NIKICHBOT)

    @PostConstruct
    fun setupHandlers() {
        registerCommand("/memstatus") { _, _ ->
            tgOperations.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
            true
        }
    }

    @CommandHandlerA("/memstatus")
    suspend fun printMemoryStatus(): Boolean {
        tgOperations.replyToCurrentMessage(memoryTrackerService.getMemoryStatus().prettyPrint())
        return true
    }
}

@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHandlerA(val value: String)

@Component
class CommandHandlerScanner(
    private val commandHandlersV2: List<CommandHandlerV2>,
) {

    @PostConstruct
    fun test() {
        val handlers = getHandlers()
        println()
    }

    fun getHandlers(): Set<SingleCommandHandler> {
        val result = mutableSetOf<SingleCommandHandler>()

        for (handler in commandHandlersV2) {
            handler::class.declaredFunctions
                .filter { it.hasAnnotation<CommandHandlerA>() }
                .map {
                    SingleCommandHandler(
                        command = it.findAnnotation<CommandHandlerA>()!!.value,
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
    val handler: CommandHandlerV2,
)

@Component
class CommandHandlerExecutor {

    suspend fun execute(handler: SingleCommandHandler, args: List<String>, update: Update): Boolean {
        val executionArgs = mutableListOf<Any>()
        for (parameter in handler.function.parameters) {
            when (parameter.type) {

                List::class -> executionArgs.add(args)
                Update::class -> executionArgs.add(update)
            }
        }
        return handler.function.call(handler.handler, executionArgs, update) as Boolean
    }

}