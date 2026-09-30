package com.nikichxp.tgbot.dashboard.config

import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jose.util.DefaultResourceRetriever
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class TelegramOidcConfig {

    /** Telegram's public signing keys; cached and refreshed by Nimbus (also on an unknown `kid`). */
    @Bean
    fun telegramJwkSource(): JWKSource<SecurityContext> =
        JWKSourceBuilder.create<SecurityContext>(
            URI(JWKS_URL).toURL(),
            DefaultResourceRetriever(5_000, 5_000, 64 * 1024)
        )
            .retrying(true)
            .build()

    companion object {
        const val ISSUER = "https://oauth.telegram.org"
        const val JWKS_URL = "https://oauth.telegram.org/.well-known/jwks.json"
    }
}
