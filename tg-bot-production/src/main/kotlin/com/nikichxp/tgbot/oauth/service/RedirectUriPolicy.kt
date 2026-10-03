package com.nikichxp.tgbot.oauth.service

import java.net.URI

object RedirectUriPolicy {

    private val loopbackHosts = setOf("localhost", "127.0.0.1", "[::1]")
    private const val MAX_LENGTH = 2048

    fun requireValid(redirectUri: String): String {
        val uri = try {
            URI(redirectUri)
        } catch (e: Exception) {
            throw IllegalArgumentException("Redirect URI is malformed: $redirectUri")
        }
        require(redirectUri.length <= MAX_LENGTH) { "Redirect URI is too long" }
        require(uri.isAbsolute && uri.host != null) { "Redirect URI must be absolute: $redirectUri" }
        require(uri.rawFragment == null) { "Redirect URI must not contain a fragment: $redirectUri" }
        require(uri.rawUserInfo == null) { "Redirect URI must not contain user info: $redirectUri" }
        val isHttps = uri.scheme.equals("https", ignoreCase = true)
        val isLoopbackHttp = uri.scheme.equals("http", ignoreCase = true) && uri.host.lowercase() in loopbackHosts
        require(isHttps || isLoopbackHttp) { "Redirect URI must use https (http only for localhost): $redirectUri" }
        return redirectUri
    }

    fun originOf(redirectUri: String): String {
        val uri = URI(redirectUri)
        val port = if (uri.port == -1) "" else ":${uri.port}"
        return "${uri.scheme.lowercase()}://${uri.host.lowercase()}$port"
    }
}
