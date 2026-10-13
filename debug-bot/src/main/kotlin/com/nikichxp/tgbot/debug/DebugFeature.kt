package com.nikichxp.tgbot.debug

import com.nikichxp.bot.common.handlers.BotFeature

object DebugFeature : BotFeature(
    id = "debug",
    title = "Debug",
    description = "/ping, /uptime, /myid, keyboard demos; admin only: /version, /memstatus"
)
