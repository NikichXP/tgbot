package com.nikichxp.tgbot.oauth.error

class OAuthException(
    val error: String,
    val description: String,
    val httpStatus: Int = 400
) : RuntimeException(description) {

    companion object {
        fun invalidRequest(description: String) = OAuthException("invalid_request", description)
        fun invalidClient() = OAuthException("invalid_client", "Client authentication failed", 401)
        fun invalidGrant(description: String) = OAuthException("invalid_grant", description)
        fun unsupportedGrantType() = OAuthException("unsupported_grant_type", "Only authorization_code is supported")
        fun accessDenied(description: String) = OAuthException("access_denied", description, 401)
    }
}
