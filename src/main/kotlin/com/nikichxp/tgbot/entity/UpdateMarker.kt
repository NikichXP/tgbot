package com.nikichxp.tgbot.entity

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.util.getMarkers
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.util.function.Function
import javax.annotation.PostConstruct

enum class UpdateMarker(val predicate: Function<Update, Any?>) {

    MESSAGE_IN_GROUP({ it.message?.chat?.id?.let { i -> i < 0 } }),
    MESSAGE_IN_CHAT({ it.message?.chat?.id?.let { i -> i > 0 } }),
    MESSAGE({ it.message }),
    EDIT_MESSAGE({ it.editedMessage }),

    REPLY({ it.message?.replyToMessage }),
    MESSAGE_WITH_TEXT({ it.message?.text }),
    MESSAGE_WITH_STICKER({ it.message?.sticker }),
    FORWARDED_MESSAGE({ if (it.message?.forwardFrom != null || it.message?.forwardFromChat != null) true else null }),
    SINGLE_ATTACHMENT({ it.message?.document }),
    PHOTOS({ it.message?.photo })

}