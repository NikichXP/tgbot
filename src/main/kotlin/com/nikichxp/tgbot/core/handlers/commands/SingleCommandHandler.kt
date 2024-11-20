package com.nikichxp.tgbot.core.handlers.commands

import kotlin.reflect.KFunction

class SingleCommandHandler(
    val command: String,
    val function: KFunction<*>,
    val handler: CommandHandler,
)