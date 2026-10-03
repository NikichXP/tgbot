package com.nikichxp.tgbot.oauth.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nikichxp.tgbot.oauth.dto.OAuthAuthorizePage
import com.nikichxp.tgbot.oauth.dto.OAuthResponseMode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OAuthAuthorizePageRendererTest {

    private val renderer = OAuthAuthorizePageRenderer(jacksonObjectMapper())

    private fun page(
        clientName: String = "Pancake Club",
        responseMode: OAuthResponseMode = OAuthResponseMode.WEB_MESSAGE,
        frameAncestors: List<String> = listOf("https://pancakes.example.com", "http://localhost:5173")
    ) = OAuthAuthorizePage("360838312", "nonce-1", clientName, responseMode, frameAncestors)

    @Test
    fun `injects config so that client name cannot break out of the script element`() {
        val html = renderer.render(page(clientName = "</script><script>alert(1)</script>"))
        assertThat(html).doesNotContain("</script><script>alert(1)")
        assertThat(html).contains("\\u003c/script\\u003e")
        assertThat(html).contains("\"nonce\":\"nonce-1\"")
        assertThat(html).contains("\"responseMode\":\"web_message\"")
        assertThat(html).doesNotContain("__OAUTH_CONFIG__")
    }

    @Test
    fun `web_message page may be framed only by the client origins`() {
        assertThat(renderer.contentSecurityPolicy(page()))
            .isEqualTo("frame-ancestors https://pancakes.example.com http://localhost:5173")
    }

    @Test
    fun `redirect page may not be framed`() {
        assertThat(renderer.contentSecurityPolicy(page(responseMode = OAuthResponseMode.QUERY)))
            .isEqualTo("frame-ancestors 'none'")
    }
}
