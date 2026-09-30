package com.nikichxp.tgbot.oauth.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikichxp.tgbot.oauth.dto.OAuthAuthorizePage
import com.nikichxp.tgbot.oauth.dto.OAuthResponseMode
import com.nikichxp.tgbot.oauth.service.OAuthAuthorizationService
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@Component
class OAuthAuthorizePageRenderer(private val objectMapper: ObjectMapper) {

    private val template: String by lazy {
        ClassPathResource(TEMPLATE_PATH).inputStream.use { String(it.readAllBytes()) }
    }

    fun render(page: OAuthAuthorizePage): String {
        val config = mapOf(
            "telegramClientId" to page.telegramClientId,
            "nonce" to page.nonce,
            "clientName" to page.clientName,
            "responseMode" to page.responseMode,
            "reloadAfterMs" to OAuthAuthorizationService.LOGIN_REQUEST_TTL.minusMinutes(1).toMillis()
        )
        return template.replace(CONFIG_PLACEHOLDER, safeForScriptElement(objectMapper.writeValueAsString(config)))
    }

    fun contentSecurityPolicy(page: OAuthAuthorizePage): String {
        val ancestors = when (page.responseMode) {
            OAuthResponseMode.WEB_MESSAGE -> page.frameAncestors.joinToString(" ").ifEmpty { "'none'" }
            OAuthResponseMode.QUERY -> "'none'"
        }
        return "frame-ancestors $ancestors"
    }

    private fun safeForScriptElement(json: String): String = json
        .replace("<", "\\u003c")
        .replace(">", "\\u003e")
        .replace("&", "\\u0026")
        .replace(" ", "\\u2028")
        .replace(" ", "\\u2029")

    companion object {
        private const val TEMPLATE_PATH = "oauth/authorize.html"
        private const val CONFIG_PLACEHOLDER = "__OAUTH_CONFIG__"
    }
}
