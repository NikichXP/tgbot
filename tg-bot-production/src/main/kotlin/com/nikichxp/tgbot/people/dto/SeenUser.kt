package com.nikichxp.tgbot.people.dto

import com.nikichxp.bot.common.entity.model.ChatModel
import com.nikichxp.bot.common.entity.model.UserModel

data class SeenUser(val user: UserModel, val botName: String, val chat: ChatModel?)
