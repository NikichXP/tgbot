package com.nikichxp.tgbot.core.service.tgapi.executor

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.core.error.TgBadRequestException
import com.nikichxp.tgbot.core.error.TgConflictException
import com.nikichxp.tgbot.core.error.TgForbiddenException
import com.nikichxp.tgbot.core.error.TgMessageTooLongException
import com.nikichxp.tgbot.core.error.TgNotFoundException
import com.nikichxp.tgbot.core.error.TgServerErrorException
import com.nikichxp.tgbot.core.error.TgTooManyRequestsException
import com.nikichxp.tgbot.core.error.TgUnauthorizedException
import com.nikichxp.tgbot.core.error.TgUnexpectedApiErrorException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TgApiErrorsTest {

    private val objectMapper = ObjectMapper()

    @Test
    fun `each telegram error code maps to its own exception`() {
        assertThat(tgApiErrorFor("m", 400, "Bad Request: chat not found")).isExactlyInstanceOf(TgBadRequestException::class.java)
        assertThat(tgApiErrorFor("m", 400, "Bad Request: message is too long")).isInstanceOf(TgMessageTooLongException::class.java)
        assertThat(tgApiErrorFor("m", 401, "Unauthorized")).isInstanceOf(TgUnauthorizedException::class.java)
        assertThat(tgApiErrorFor("m", 403, "Forbidden: bot was blocked by the user")).isInstanceOf(TgForbiddenException::class.java)
        assertThat(tgApiErrorFor("m", 404, "Not Found")).isInstanceOf(TgNotFoundException::class.java)
        assertThat(tgApiErrorFor("m", 409, "Conflict")).isInstanceOf(TgConflictException::class.java)
        assertThat(tgApiErrorFor("m", 429, "Too Many Requests")).isInstanceOf(TgTooManyRequestsException::class.java)
        assertThat(tgApiErrorFor("m", 502, "Bad Gateway")).isInstanceOf(TgServerErrorException::class.java)
        assertThat(tgApiErrorFor("m", 418, "teapot")).isInstanceOf(TgUnexpectedApiErrorException::class.java)
    }

    @Test
    fun `description is read from telegram json body and falls back to raw body`() {
        val fromJson = tgApiErrorFor("sendMessage", 400, """{"ok":false,"error_code":400,"description":"Bad Request: x"}""", objectMapper)
        val fromHtml = tgApiErrorFor("sendMessage", 502, "<html>Bad Gateway</html>", objectMapper)

        assertThat(fromJson.description).isEqualTo("Bad Request: x")
        assertThat(fromJson.errorCode).isEqualTo(400)
        assertThat(fromHtml.description).isEqualTo("<html>Bad Gateway</html>")
    }

    @Test
    fun `rate limit carries retry after from telegram parameters`() {
        val body = """{"ok":false,"error_code":429,"description":"Too Many Requests: retry after 30","parameters":{"retry_after":30}}"""

        val error = tgApiErrorFor("sendMessage", 429, body, objectMapper)

        assertThat(error).isInstanceOf(TgTooManyRequestsException::class.java)
        assertThat((error as TgTooManyRequestsException).retryAfterSeconds).isEqualTo(30)
    }
}
