package com.nikichxp.tgbot.oauth.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RedirectUriPolicyTest {

    @Test
    fun `accepts https and loopback http`() {
        assertThat(RedirectUriPolicy.requireValid("https://pancakes.example.com/auth/callback"))
            .isEqualTo("https://pancakes.example.com/auth/callback")
        assertThat(RedirectUriPolicy.requireValid("http://localhost:5173/callback"))
            .isEqualTo("http://localhost:5173/callback")
    }

    @Test
    fun `rejects plain http on public hosts`() {
        assertThatThrownBy { RedirectUriPolicy.requireValid("http://pancakes.example.com/callback") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `rejects relative uris, fragments, user info and other schemes`() {
        listOf(
            "/callback",
            "https://pancakes.example.com/callback#frag",
            "https://user@pancakes.example.com/callback",
            "javascript:alert(1)",
            "https://exa mple.com"
        ).forEach { uri ->
            assertThatThrownBy { RedirectUriPolicy.requireValid(uri) }
                .`as`(uri)
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun `origin keeps explicit port and drops path and query`() {
        assertThat(RedirectUriPolicy.originOf("https://PANCAKES.example.com/a/b?c=d")).isEqualTo("https://pancakes.example.com")
        assertThat(RedirectUriPolicy.originOf("http://localhost:5173/cb")).isEqualTo("http://localhost:5173")
    }
}
