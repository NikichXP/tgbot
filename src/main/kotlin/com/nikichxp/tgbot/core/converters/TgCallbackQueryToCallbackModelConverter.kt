package com.nikichxp.tgbot.core.converters

import com.nikichxp.tgbot.core.dto.CallbackQuery
import com.nikichxp.tgbot.core.entity.common.CallbackModel
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TgCallbackQueryToCallbackModelConverter : Converter<CallbackQuery, CallbackModel> {

    override fun convert(source: CallbackQuery): CallbackModel? {
        val message = source.message ?: return null
        return CallbackModel(
            userId = source.from.id,
            data = source.data,
            messageText = message.text,
            buttonText = message.replyMarkup?.inlineKeyboard?.flatten()
                ?.find { it.callbackData == source.data }?.text,
            chatId = message.chat.id,
            messageId = message.messageId
        )
    }
}
