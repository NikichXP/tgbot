package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.service.TgOperations
import org.springframework.stereotype.Service

@Service
class StickerReplyHandler(
    private val tgOperations: TgOperations
) : UpdateHandler {
    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.REPLY, UpdateMarker.HAS_STICKER)

    override fun handleUpdate(update: Update) {
        tgOperations.sendMessage(update.getContextChatId()!!.toString(), "I CAN SEE THE STICKER REACTION!!!")
    }
}