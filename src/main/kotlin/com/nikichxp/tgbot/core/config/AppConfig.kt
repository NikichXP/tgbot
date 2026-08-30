package com.nikichxp.tgbot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
open class AppConfig(
    var adminId: Long = 0L,
    var adminBot: String? = null,
    var webhook: String = "",
    var localEnv: Boolean = false,
    var tokens: Tokens = Tokens(), // TODO verify that I can delete this
    var tracer: Tracer = Tracer(),
    var openRouter: OpenRouter = OpenRouter(),
    var discord: Discord = Discord(),
    var trustedUsers: List<String> = emptyList()
) {

    var suspendBotRegistering: Boolean = false

    // TODO change registration of the bots to be dynamic | store in DB?
    companion object {
        class Discord(
            var publicKey: String? = null
        )

        class Tokens(
            var nikichBot: String? = null,
            var allMyStuffBot: String? = null,
            var santaBot: String? = null,
            var demoBot: String? = null,
            var childTrackerBot: String? = null
        )

        class Tracer(
            var store: Boolean = false,
            var ttl: Long = 1,
            var capacity: Int = 100,
            var token: String? = null // leave null to disable viewing of traces
        )

        class OpenRouter(
            var apiKey: String = "",
            var defaultModel: String = "openai/gpt-4o-mini",
            var baseUrl: String = "https://openrouter.ai/api/v1",
            var referer: String = "https://bot.nikichxp.xyz",
            var title: String = "TGBot",
            var transcriptionModel: String = "openai/whisper-1"
        )
    }
}
