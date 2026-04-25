package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.dto.Update
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ChatUpdatesToPromptSerializerService {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // [10:45] Bob: yes, exactly that!  ↩ replying to Alice: "are we meeting at 6pm?"
    fun serialize(updates: List<LoggedMessage>): String {
        return updates.joinToString("\n") { loggedMessage ->
            val message = getMessage(loggedMessage.update)
            val time = loggedMessage.time.format(timeFormatter)
            val author = getAuthorName(loggedMessage.update)
            val text = getMessageText(message)

            val replyPart = message?.replyToMessage?.let { replyTo ->
                val replyAuthor = getAuthorNameFromMessage(replyTo)
                val replyText = getMessageText(replyTo)
                "  ↩ replying to $replyAuthor: \"$replyText\""
            } ?: ""

            "[$time] $author: $text$replyPart"
        }
    }

    fun getAuthorName(update: Update): String {
        val message = getMessage(update)
        return getAuthorNameFromMessage(message)
    }

    private fun getMessage(update: Update): com.nikichxp.tgbot.core.dto.Message? {
        return update.message ?: update.editedMessage ?: update.channelPost ?: update.editedChannelPost
    }

    private fun getMessageText(message: com.nikichxp.tgbot.core.dto.Message?): String {
        return message?.text ?: message?.sticker?.emoji ?: ""
    }

    private fun getAuthorNameFromMessage(message: com.nikichxp.tgbot.core.dto.Message?): String {
        val user = message?.from
        return if (user != null) {
            user.username?.let { "@$it" } ?: listOfNotNull(user.firstName, user.lastName).joinToString(" ")
        } else {
            "Unknown"
        }
    }
}