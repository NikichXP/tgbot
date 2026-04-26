package com.nikichxp.tgbot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
open class AppConfig(
    var adminId: Long,
    var adminBot: String?,
    var webhook: String,
    var localEnv: Boolean,
    var tokens: Tokens, // TODO verify that I can delete this
    var tracer: Tracer,
    var openRouter: OpenRouter,
    var trustedUsers: List<String> = emptyList()
) {

    var suspendBotRegistering: Boolean = false

    // TODO change registration of the bots to be dynamic | store in DB?
    companion object {
        class Tokens(
            var nikichBot: String?,
            var allMyStuffBot: String?,
            var santaBot: String?,
            var demoBot: String?,
            var childTrackerBot: String?
        )

        class Tracer(
            var store: Boolean = false,
            var ttl: Long = 1,
            var capacity: Int = 100,
            var token: String? = null // leave null to disable viewing of traces
        )

        class OpenRouter(
            var apiKey: String,
            var defaultModel: String,
            var baseUrl: String,
            var referer: String,
            var title: String
        )
    }
}
