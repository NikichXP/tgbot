package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.summary.entity.LoggedMessage
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ChatUpdatesToPromptSerializerService {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // [10:45] Bob: yes, exactly that!  ↩ replying to Alice: "are we meeting at 6pm?"
    fun serialize(updates: List<LoggedMessage>): String {
        return updates.joinToString("\n") { loggedMessage ->
            val time = loggedMessage.time.format(timeFormatter)
            val (author, text, replyPart) = describe(loggedMessage)

            "[$time] $author: $text$replyPart"
        }
    }

    private data class MessageDescription(val author: String, val text: String, val replyPart: String)

    private fun describe(loggedMessage: LoggedMessage): MessageDescription {
        val author = loggedMessage.authorName ?: "Unknown"
        val text = loggedMessage.text ?: ""
        val replyPart = loggedMessage.replyText?.let {
            "  ↩ replying to ${loggedMessage.replyAuthorName ?: "Unknown"}: \"$it\""
        } ?: ""
        return MessageDescription(author, text, replyPart)
    }
}