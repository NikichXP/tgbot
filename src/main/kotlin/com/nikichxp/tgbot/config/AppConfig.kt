package com.nikichxp.tgbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
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