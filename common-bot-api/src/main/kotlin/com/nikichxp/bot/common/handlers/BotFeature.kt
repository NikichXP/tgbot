package com.nikichxp.bot.common.handlers

open class BotFeature(
    val id: String,
    val title: String,
    val description: String = ""
) {

    override fun toString() = id
}
