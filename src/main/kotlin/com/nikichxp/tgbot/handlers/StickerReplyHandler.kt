package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.dto.dice.DiceEmoji
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.service.TgOperations
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class StickerReplyHandler(
    private val tgOperations: TgOperations,
    private val mongoTemplate: MongoTemplate
) : UpdateHandler {
    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.REPLY, UpdateMarker.HAS_STICKER)

    override fun handleUpdate(update: Update) {
        val (fromId, toId) = update.getMembers().let {
            it.author?.id to it.target?.id
        }
        val emoji = update.message?.sticker?.emoji

        if (fromId == null || toId == null || emoji == null) {
            return
        }

        runBlocking {
            launch {
                mongoTemplate.save(StickerReaction(
                    from = fromId,
                    to = toId,
                    emoji = emoji
                ))
            }
        }

        tgOperations.sendMessage(
            update.getContextChatId()!!.toString(),
            "I CAN SEE THE STICKER REACTION! The reaction is: ${emoji}",
            replyToMessageId = update.message.messageId
        )
    }
}

data class StickerReaction(
    val emoji: String,
    val from: Long,
    val to: Long,
    val date: Instant = Instant.now()
) {
    var id: String = UUID.randomUUID().toString()
}