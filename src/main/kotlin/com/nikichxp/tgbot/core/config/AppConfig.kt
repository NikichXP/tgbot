package com.nikichxp.tgbot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class AppConfig(
    var adminId: Long,
    var webhook: String,
    var localEnv: Boolean,
    var tokens: Tokens,
    var tracer: Tracer
) {

    var suspendBotRegistering: Boolean = false

    companion object {
        class Tokens(
            var nikichBot: String?,
            var allMyStuffBot: String?,
            var santaBot: String?,
            var demoBot: String?
        )

        class Tracer(
            var store: Boolean = false,
            var ttl: Long = 1
        )
    }
}