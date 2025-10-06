package com.nikichxp.tgbot.core.entity

import com.nikichxp.tgbot.core.dto.Update
import java.util.function.Function

enum class UpdateMarker(val predicate: Function<Update, Any?>) {

    ALL({ true }),
    MESSAGE_IN_GROUP({ it.message?.chat?.id?.let { i -> i < 0 } }),
    MESSAGE_IN_CHAT({ it.message?.chat?.id?.let { i -> i > 0 } }),
    MESSAGE({ it.message }),
    EDIT_MESSAGE({ it.editedMessage }),

    IS_COMMAND({ it.message?.text?.startsWith("/") }),
    IS_NOT_COMMAND({ it.message?.text?.startsWith("/")?.not() }),

    REPLY({ it.message?.replyToMessage }),
    IS_NOT_REPLY({ it.message?.replyToMessage == null }),
    HAS_TEXT({ it.message?.text }),
    HAS_STICKER({ it.message?.sticker }),
    HAS_CALLBACK({ it.callbackQuery }),
    FORWARDED_MESSAGE({ if (it.message?.forwardFrom != null || it.message?.forwardFromChat != null) true else null }),
    SINGLE_ATTACHMENT({ it.message?.document }),
    PHOTOS({ it.message?.photo })

}