package com.nikichxp.tgbot.karmabot.handlers

import com.nikichxp.tgbot.core.entity.InteractionRole
import com.nikichxp.tgbot.core.entity.MessageInteractionResult
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.karmabot.service.EmojiService
import com.nikichxp.tgbot.karmabot.service.actions.LikedMessageService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class StickerReplyHandler(
    private val tgMessageService: TgMessageService,
    private val mongoTemplate: MongoTemplate,
    private val emojiService: EmojiService,
    private val likedMessageService: LikedMessageService,
) : UpdateHandler {

    override fun requiredFeatures() = setOf(Features.KARMA)
    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.REPLY, UpdateMarker.HAS_STICKER)

    override suspend fun handleUpdate(updateContext: UpdateContext) {
        val author = updateContext.from
        val target = updateContext.reply?.from
        val emoji = updateContext.message?.sticker?.emoji

        if (author == null || target == null || emoji == null) {
            return
        }

        val power = emojiService.getEmojiPower(emoji)
        if (power == null) {
            saveUnIdentifiedEmoji(author.id, target.id, emoji, updateContext)
        } else {
            val interactionResult = MessageInteractionResult(
                mutableMapOf(author to InteractionRole.ACTOR, target to InteractionRole.TARGET), power
            )
            likedMessageService.changeRating(interactionResult, updateContext)
        }
    }

    suspend fun saveUnIdentifiedEmoji(fromId: Long, toId: Long, emoji: String, context: UpdateContext) {
        runBlocking {
            launch {
                mongoTemplate.save(
                    StickerReaction(
                        from = fromId,
                        to = toId,
                        emoji = emoji,
                        chatId = context.getChatId(),
                        messageId = context.message?.id
                    )
                )
            }
        }

        tgMessageService.sendMessage {
            sendInCurrentChat()
            text = "I CAN SEE THE STICKER REACTION! The reaction is: $emoji"
        }
    }
}

data class StickerReaction(
    val emoji: String,
    val from: Long,
    val to: Long,
    val date: Instant = Instant.now(),
    var chatId: Long? = null,
    var messageId: Long? = null
) {
    var id: String = UUID.randomUUID().toString()
}