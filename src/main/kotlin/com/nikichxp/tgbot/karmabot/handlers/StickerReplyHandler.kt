package com.nikichxp.tgbot.karmabot.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.convertToMessageIntResult
import com.nikichxp.tgbot.core.util.getContextChatId
import com.nikichxp.tgbot.core.util.getContextMessageId
import com.nikichxp.tgbot.core.util.getMembers
import com.nikichxp.tgbot.karmabot.service.EmojiService
import com.nikichxp.tgbot.karmabot.service.actions.LikedMessageService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class StickerReplyHandler(
    private val tgOperations: TgOperations,
    private val mongoTemplate: MongoTemplate,
    private val emojiService: EmojiService,
    private val likedMessageService: LikedMessageService,
    private val objectMapper: ObjectMapper
) : UpdateHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun requiredFeatures() = setOf(Features.KARMA)
    override fun getMarkers(): Set<UpdateMarker> = setOf(UpdateMarker.REPLY, UpdateMarker.HAS_STICKER)

    override suspend fun handleUpdate(update: Update) {
        val members = update.getMembers() ?: return
        val (fromId, toId) = members.let {
            it.author?.id to it.target?.id
        }
        val emoji = update.message?.sticker?.emoji

        if (fromId == null || toId == null || emoji == null) {
            return
        }

        val power = emojiService.getEmojiPower(emoji)
        if (power == null) {
            saveUnIdentifiedEmoji(fromId, toId, emoji, update)
        } else {
            val interactionResult = update.convertToMessageIntResult(power) ?: let {
                // TODO make some fancy logger here
                //  that sends all the errors to tg chat bot
                //  and can be accessed with web interface
                logger.error(
                    "Message cannot be converted to interaction result, whatever: " +
                            objectMapper.writeValueAsString(update)
                )
                null
            } ?: return
            likedMessageService.changeRating(interactionResult, update)
        }
    }

    suspend fun saveUnIdentifiedEmoji(fromId: Long, toId: Long, emoji: String, update: Update) {
        runBlocking {
            launch {
                mongoTemplate.save(
                    StickerReaction(
                        from = fromId,
                        to = toId,
                        emoji = emoji,
                        chatId = update.getContextChatId(),
                        messageId = update.getContextMessageId()
                    )
                )
            }
        }

        tgOperations.sendMessage {
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