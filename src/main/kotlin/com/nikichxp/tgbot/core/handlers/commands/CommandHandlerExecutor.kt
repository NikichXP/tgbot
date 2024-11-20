package com.nikichxp.tgbot.core.handlers.commands

import com.nikichxp.tgbot.core.dto.Update
import org.springframework.stereotype.Component
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

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