package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgUpdateContext
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.entity.common.CallbackModel
import com.nikichxp.tgbot.core.entity.common.ChatModel
import com.nikichxp.tgbot.core.entity.common.MessageModel
import com.nikichxp.tgbot.core.entity.common.ReplyModel
import com.nikichxp.tgbot.core.entity.common.UserModel
import com.nikichxp.tgbot.core.util.getMentionedMessage
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Component

@Component
class TgUpdateContextMapper(
    private val conversionService: ConversionService
) {

    fun mapToUpdateContext(update: Update, bot: TgBotInfo): TgUpdateContext {
        val updateContext = TgUpdateContext(bot)

        val mentionedMessage = update.getMentionedMessage()

        updateContext.updateSeqId = update.updateId
        updateContext.chat = conversionService.convert(mentionedMessage?.chat, ChatModel::class.java)
        updateContext.from = conversionService.convert(mentionedMessage?.from, UserModel::class.java)
        updateContext.reply = conversionService.convert(mentionedMessage?.replyToMessage, ReplyModel::class.java)
        updateContext.message = conversionService.convert(mentionedMessage, MessageModel::class.java)
        updateContext.callback = conversionService.convert(update.callbackQuery, CallbackModel::class.java)
        updateContext.markers = mapMarkers(update)

        return updateContext
    }

    private fun mapMarkers(update: Update): Set<UpdateMarker> {
        return UpdateMarker.entries.filter {
            val result = it.predicate.apply(update)
            result as? Boolean ?: (result != null)
        }.toSet()
    }
}
