package com.nikichxp.tgbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "app")
@ConstructorBinding
class AppConfig(
    var adminId: Long,
    var webhook: String,
    var tokens: Tokens,
    var tracer: Tracer
)