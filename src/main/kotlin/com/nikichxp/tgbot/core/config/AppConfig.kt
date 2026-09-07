package com.nikichxp.tgbot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
open class AppConfig(
    var adminId: Long = 0L,
    var adminBot: String? = null,
    var webhook: String = "",
    var localEnv: Boolean = false,
    var tracer: Tracer = Tracer(),
    var openRouter: OpenRouter = OpenRouter(),
    var discord: Discord = Discord(),
    var trustedUsers: List<String> = emptyList()
) {

    var suspendBotRegistering: Boolean = false

    companion object {
        class Discord(
            var publicKey: String? = null
        )

        class Tracer(
            var store: Boolean = false,
            var ttl: Long = 1,
            var capacity: Int = 100,
            var token: String? = null // leave null to disable viewing of traces
        )

        class OpenRouter(
            var apiKey: String = "",
            var defaultModel: String = "openrouter/auto",
            var baseUrl: String = "https://openrouter.ai/api/v1",
            var referer: String = "https://github.com/NikichXP/tgbot",
            var title: String = "NikichXP TG Bot",
            var transcriptionModel: String = "openai/whisper-1"
        )
    }
}
