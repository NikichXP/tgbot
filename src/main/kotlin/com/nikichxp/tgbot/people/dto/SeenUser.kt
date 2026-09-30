package com.nikichxp.tgbot.people.dto

import com.nikichxp.tgbot.core.entity.common.ChatModel
import com.nikichxp.tgbot.core.entity.common.UserModel

data class SeenUser(val user: UserModel, val botName: String, val chat: ChatModel?)
