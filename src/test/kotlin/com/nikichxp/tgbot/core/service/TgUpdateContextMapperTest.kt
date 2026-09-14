package com.nikichxp.tgbot.core.service

import com.nikichxp.tgbot.core.dto.CallbackQuery
import com.nikichxp.tgbot.core.dto.Chat
import com.nikichxp.tgbot.core.dto.InlineKeyboardMarkup
import com.nikichxp.tgbot.core.dto.Message
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.dto.User
import com.nikichxp.tgbot.core.dto.files.Voice
import com.nikichxp.tgbot.core.dto.keyboard.InlineKeyboardButton
import com.nikichxp.tgbot.core.dto.stickers.Sticker
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TgUpdateContextMapperTest {

    private val mapper = TgUpdateContextMapper()
    private val bot = TgBotInfo("testbot", setOf())

    @Test
    fun `maps a plain text message`() {
        val context = mapper.mapToUpdateContext(updateOf(messageOf(text = "hello")), bot)

        assertThat(context.message?.id).isEqualTo(MESSAGE_ID)
        assertThat(context.message?.text).isEqualTo("hello")
        assertThat(context.chat?.id).isEqualTo(CHAT_ID)
        assertThat(context.from?.id).isEqualTo(USER_ID)
        assertThat(context.from?.fullName).isEqualTo("John Doe")
        assertThat(context.getChatId()).isEqualTo(CHAT_ID)
    }

    @Test
    fun `maps a voice message that has no text`() {
        val voice = Voice(fileId = "file-1", fileUniqueId = "uniq-1", duration = 7, mimeType = "audio/ogg")

        val context = mapper.mapToUpdateContext(updateOf(messageOf(text = null, voice = voice)), bot)

        assertThat(context.message).isNotNull()
        assertThat(context.message?.text).isNull()
        assertThat(context.message?.voice?.fileId).isEqualTo("file-1")
        assertThat(context.message?.voice?.duration).isEqualTo(7)
    }

    @Test
    fun `maps a sticker message that has no text`() {
        val sticker = Sticker(
            fileId = "sticker-1",
            fileUniqueId = "uniq-2",
            width = 1,
            height = 1,
            isAnimated = false,
            isVideo = false,
            emoji = "\uD83D\uDC4D"
        )

        val context = mapper.mapToUpdateContext(updateOf(messageOf(text = null, sticker = sticker)), bot)

        assertThat(context.message).isNotNull()
        assertThat(context.message?.sticker?.emoji).isEqualTo("\uD83D\uDC4D")
    }

    @Test
    fun `keeps the replied-to message id when the reply has no author`() {
        val reply = messageOf(messageId = 41, text = "original", from = null)

        val context = mapper.mapToUpdateContext(updateOf(messageOf(replyToMessage = reply)), bot)

        assertThat(context.reply).isNotNull()
        assertThat(context.reply?.messageId).isEqualTo(41)
        assertThat(context.reply?.text).isEqualTo("original")
        assertThat(context.reply?.from).isNull()
        assertThat(context.reply?.chat?.id).isEqualTo(CHAT_ID)
    }

    @Test
    fun `resolves the pressed button text from the keyboard`() {
        val markup = InlineKeyboardMarkup(listOf(listOf(button("Yes", "btn-yes"), button("No", "btn-no"))))
        val update = callbackUpdateOf(data = "btn-no", replyMarkup = markup)

        val context = mapper.mapToUpdateContext(update, bot)

        assertThat(context.callback?.buttonText).isEqualTo("No")
        assertThat(context.callback?.data).isEqualTo("btn-no")
        assertThat(context.callback?.chatId).isEqualTo(CHAT_ID)
        assertThat(context.callback?.userId).isEqualTo(USER_ID)
    }

    @Test
    fun `leaves button text null when the keyboard no longer contains the callback`() {
        val markup = InlineKeyboardMarkup(listOf(listOf(button("Yes", "btn-yes"))))
        val update = callbackUpdateOf(data = "btn-stale", replyMarkup = markup)

        val context = mapper.mapToUpdateContext(update, bot)

        assertThat(context.callback).isNotNull()
        assertThat(context.callback?.buttonText).isNull()
    }

    @Test
    fun `leaves button text null when the message has no keyboard at all`() {
        val context = mapper.mapToUpdateContext(callbackUpdateOf(data = "btn-yes", replyMarkup = null), bot)

        assertThat(context.callback).isNotNull()
        assertThat(context.callback?.buttonText).isNull()
    }

    @Test
    fun `populates markers`() {
        val context = mapper.mapToUpdateContext(updateOf(messageOf(text = "/ping")), bot)

        assertThat(context.markers).contains(UpdateMarker.MESSAGE, UpdateMarker.HAS_TEXT, UpdateMarker.IS_COMMAND)
        assertThat(context.markers).doesNotContain(UpdateMarker.IS_NOT_COMMAND, UpdateMarker.HAS_CALLBACK)
    }

    private fun updateOf(message: Message) = Update(updateId = 1, message = message)

    private fun callbackUpdateOf(data: String, replyMarkup: InlineKeyboardMarkup?) = Update(
        updateId = 1,
        callbackQuery = CallbackQuery(
            id = "cb-1",
            from = user(),
            message = messageOf(text = "pick one", replyMarkup = replyMarkup),
            data = data,
            chatInstance = "instance-1"
        )
    )

    private fun messageOf(
        messageId: Long = MESSAGE_ID,
        text: String? = "text",
        from: User? = user(),
        voice: Voice? = null,
        sticker: Sticker? = null,
        replyToMessage: Message? = null,
        replyMarkup: InlineKeyboardMarkup? = null
    ) = Message(
        messageId = messageId,
        from = from,
        date = 0,
        chat = Chat(id = CHAT_ID, type = "supergroup", title = "Test Chat"),
        text = text,
        voice = voice,
        sticker = sticker,
        replyToMessage = replyToMessage,
        replyMarkup = replyMarkup
    )

    private fun user() = User(id = USER_ID, isBot = false, firstName = "John", lastName = "Doe", username = "johnd")

    private fun button(text: String, callbackData: String) = InlineKeyboardButton().apply {
        this.text = text
        this.callbackData = callbackData
    }

    companion object {
        private const val CHAT_ID = -1001L
        private const val MESSAGE_ID = 42L
        private const val USER_ID = 777L
    }
}
