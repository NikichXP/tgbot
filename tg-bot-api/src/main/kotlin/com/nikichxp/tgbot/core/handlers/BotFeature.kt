package com.nikichxp.tgbot.core.handlers

open class BotFeature(
    val id: String,
    val title: String,
    val description: String = ""
) {

    override fun toString() = id
}
