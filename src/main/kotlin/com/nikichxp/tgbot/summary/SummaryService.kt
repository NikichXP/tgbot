package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.util.AppStorage
import com.nikichxp.tgbot.summary.ai.LLMProvider
import com.nikichxp.tgbot.summary.ai.LLMRequest
import com.nikichxp.tgbot.summary.entity.LoggedMessage
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class SummaryService(
    private val summaryMessageStorageService: SummaryMessageStorageService,
    private val appStorage: AppStorage,
    private val chatUpdatesToPromptSerializerService: ChatUpdatesToPromptSerializerService,
    private val llmProvider: LLMProvider
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Cacheable("summary_feature")
    fun getFeatureEnabledStatus(chatId: Long): Boolean {
        return appStorage.getData("summary_feature_$chatId")?.value?.toBoolean() ?: false
    }

    @CacheEvict("summary_feature", allEntries = true)
    fun setFeatureEnabledStatus(chatId: Long, enabled: Boolean) {
        appStorage.saveData("summary_feature_$chatId", enabled.toString())
    }

    suspend fun getRecapForToday(chatId: Long, model: String? = null): String =
        getRecapSince(chatId, getStartingPointOfToday(), model)

    suspend fun getRecapSince(chatId: Long, since: LocalDateTime, model: String? = null): String {
        val updates = getUpdatesForChatAfter(chatId, since)
        if (updates.isEmpty()) {
            return "Сегодня сообщений не было — пересказывать пока нечего."
        }

        val chatHistory = chatUpdatesToPromptSerializerService.serialize(updates)

        val response = llmProvider.complete(
            LLMRequest.of(
                model = model,
                systemPrompt = RECAP_SYSTEM_PROMPT,
                userPrompt = buildRecapUserPrompt(chatHistory),
                maxTokens = 2000
            )
        )
        logger.info(
            "Recap generated: chatId={}, messages={}, model={}, promptTokens={}, completionTokens={}",
            chatId, updates.size, response.model,
            response.usage?.promptTokens, response.usage?.completionTokens
        )
        return response.content.trim().ifBlank { "LLM вернул пустой ответ. Попробуй позже." }
    }

    private fun buildRecapUserPrompt(chatHistory: String): String = """
        Ниже — история группового чата за текущие сутки.
        Формат каждой строки: "[HH:mm] Автор: текст".
        Если сообщение было ответом, в конце добавлена пометка "↩ replying to Имя: \"...\"".

        === НАЧАЛО ИСТОРИИ ===
        $chatHistory
        === КОНЕЦ ИСТОРИИ ===

        Сделай краткий пересказ по правилам из system-промпта.
    """.trimIndent()

    private fun getUpdatesForChat(chatId: Long): List<LoggedMessage> {
        return summaryMessageStorageService.getMessages(chatId)
    }

    private fun getUpdatesForChatAfter(chatId: Long, after: LocalDateTime): List<LoggedMessage> {
        return summaryMessageStorageService.getMessagesAfter(chatId, after)
    }

    private fun getStartingPointOfToday(): LocalDateTime {
        val now = LocalDateTime.now()
        return if (now.toLocalTime().isBefore(NIGHT_SEPARATOR)) {
            now.minusDays(1).with(NIGHT_SEPARATOR)
        } else {
            now.with(NIGHT_SEPARATOR)
        }
    }

    companion object {
        private val NIGHT_SEPARATOR = LocalTime.of(4, 0)

        private val RECAP_SYSTEM_PROMPT = """
            Ты помощник, который делает краткий пересказ переписки в групповом Telegram-чате.

            Правила:
            - Пиши на русском языке, нейтральным дружелюбным тоном.
            - Группируй сообщения по темам/обсуждениям, а не по хронологии.
            - Для каждой темы дай 1–3 предложения: о чём шла речь, к чему пришли (если пришли), участники (если имеет значение).
            - Упоминай авторов так же, как они обозначены в истории (@username или Имя).
            - Сохраняй важные детали: договорённости, решения, ссылки, числа, даты, встречи.
            - Игнорируй флуд, стикеры без контекста и односложные реплики, если они не меняют смысл.
            - Не выдумывай факты, которых нет в переписке. Если чего-то не понял — пропусти.
            - Не цитируй переписку целиком, но короткие яркие цитаты (1–2 на сводку) уместны, если они характеризуют разговор.
            - Не пиши о политических темах.

            Формат ответа (Markdown для Telegram):
            *Краткая сводка*

            *<Название темы>*
            <Развёрнутый пересказ обсуждения: аргументы, участники, итог. 3–7 предложений.>

            В конце одной строкой: _Активных тем: N_.
        """.trimIndent()
    }

}
