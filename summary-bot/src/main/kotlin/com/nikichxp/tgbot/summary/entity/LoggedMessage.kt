package com.nikichxp.tgbot.summary.entity

import com.nikichxp.bot.common.entity.UpdateContext
import com.nikichxp.bot.common.util.UserFormatter
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class LoggedMessage(
    val updateContext: UpdateContext? = null,
    val authorId: Long? = null,
    val authorName: String? = null,
    val text: String? = null,
    val replyAuthorName: String? = null,
    val replyText: String? = null,
    val chatId: Long,
    val time: LocalDateTime = LocalDateTime.now()
) {
    @Id
    lateinit var id: ObjectId

    companion object {
        fun fromContext(context: UpdateContext): LoggedMessage {
            val chatId = context.chat?.id ?: throw IllegalArgumentException("Can't get chat id")
            val from = context.from
            val reply = context.reply
            return LoggedMessage(
                updateContext = context,
                authorId = from?.id,
                authorName = from?.let { UserFormatter.mention(it) },
                text = context.message?.text ?: context.message?.sticker?.emoji ?: "",
                replyAuthorName = reply?.from?.let { UserFormatter.mention(it) },
                replyText = reply?.text,
                chatId = chatId
            )
        }
    }
}