package com.nikichxp.tgbot.voicepad

import com.nikichxp.tgbot.summary.ai.LLMProvider
import com.nikichxp.tgbot.summary.ai.LLMRequest
import org.springframework.stereotype.Service

interface VoicePadProcessor {
    val command: String
    val outputFileName: String
    suspend fun process(transcriptions: List<String>): String

    companion object {
        const val CLAUDE_SONNET_MODEL = "anthropic/claude-sonnet-4-5"
    }
}

@Service
class CreatePromptProcessor(private val llmProvider: LLMProvider) : VoicePadProcessor {

    override val command = "/create-prompt"
    override val outputFileName = "prompt.md"

    override suspend fun process(transcriptions: List<String>): String {
        val combinedText = transcriptions.mapIndexed { i, t -> "[Запись ${i + 1}]\n$t" }.joinToString("\n\n")

        return llmProvider.complete(
            LLMRequest.of(
                model = VoicePadProcessor.CLAUDE_SONNET_MODEL,
                systemPrompt = PROMPT_SYSTEM,
                userPrompt = buildString {
                    appendLine("Ниже приведены расшифровки голосовых заметок разработчика.")
                    appendLine("Создай на их основе детальный промпт/план реализации.")
                    appendLine()
                    append(combinedText)
                },
                maxTokens = 8000
            )
        ).content.trim()
    }

    companion object {
        private val PROMPT_SYSTEM = """
            Ты — опытный software architect. Твоя задача — взять сырые голосовые заметки разработчика
            и превратить их в чёткий, структурированный промпт для AI-ассистента (например, Cursor, Copilot, Claude).

            Правила:
            - Выяви основную задачу/фичу и опиши её кратко в начале.
            - Разбей реализацию на логические шаги с техническими деталями.
            - Включи требования к архитектуре, если они упоминаются.
            - Учти edge cases и нефункциональные требования.
            - Укажи конкретные технологии/библиотеки/паттерны, если разработчик их называл.
            - Пиши на том же языке, на котором говорил разработчик (русский, если заметки на русском).
            - Формат вывода: Markdown с заголовками, списками и блоками кода там, где нужно.
            - Не добавляй то, чего не было в заметках — только систематизируй сказанное.

            Структура промпта:
            # Задача
            <суть в 2–4 предложениях>

            ## Контекст
            <что уже есть, технический стек, окружение>

            ## Требования
            <функциональные и нефункциональные>

            ## Шаги реализации
            <пронумерованный список с деталями>

            ## Edge cases и ограничения
            <что нужно учесть>
        """.trimIndent()
    }
}

@Service
class CreateNotepadProcessor(private val llmProvider: LLMProvider) : VoicePadProcessor {

    override val command = "/create-notepad"
    override val outputFileName = "notepad.md"

    override suspend fun process(transcriptions: List<String>): String {
        val combinedText = transcriptions.mapIndexed { i, t -> "[Запись ${i + 1}]\n$t" }.joinToString("\n\n")

        return llmProvider.complete(
            LLMRequest.of(
                model = VoicePadProcessor.CLAUDE_SONNET_MODEL,
                systemPrompt = NOTEPAD_SYSTEM,
                userPrompt = buildString {
                    appendLine("Ниже приведены расшифровки голосовых заметок.")
                    appendLine("Создай на их основе живой, интересный документ в формате лекции или рассказа.")
                    appendLine()
                    append(combinedText)
                },
                maxTokens = 8000
            )
        ).content.trim()
    }

    companion object {
        private val NOTEPAD_SYSTEM = """
            Ты — талантливый редактор и педагог. Твоя задача — взять сырые голосовые заметки
            (с оговорками, повторами, неточными формулировками) и превратить их в живое, интересное
            и познавательное чтиво в формате Markdown.

            Это может быть лекция для студентов, рассказ для друзей, статья в блог — подбери формат
            по контексту материала.

            Правила:
            - Сохраняй все ключевые идеи и факты из заметок — ничего не выдумывай.
            - Числа и конкретные данные уточни по контексту, если очевидно что оговорка. Если неясно — оставь как есть.
            - Убирай словесный мусор: "ну", "вот", "короче", повторы, оговорки.
            - Структурируй материал логично: введение → основное → выводы.
            - Используй примеры, аналогии, метафоры для объяснения сложного.
            - Пиши живым языком — не скучно, не казённо. Читатель должен получить удовольствие.
            - Технические термины на иностранном языке (Docker, API, SQL и т.д.) оставляй как есть.
            - Формат: Markdown с заголовками, списками, блоками кода если нужно.
            - Язык выходного текста — тот же, что в заметках (если заметки на русском — пиши на русском).

            Структура:
            # Название темы
            <вводный абзац — о чём пойдёт речь и почему это интересно>

            ## <Логическая секция 1>
            ...

            ## <Логическая секция 2>
            ...

            ## Выводы / Итог
            <ключевые мысли, что запомнить>
        """.trimIndent()
    }
}
