package com.nikichxp.tgbot.core.util

import com.fasterxml.jackson.annotation.JsonIgnore
import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update

@JsonIgnore
fun Update.getMentionedMessage(): Message? {
    return this.message
        ?: this.editedMessage
        ?: this.editedChannelPost
        ?: this.channelPost
        ?: this.callbackQuery?.message
}

