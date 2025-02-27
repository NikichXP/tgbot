package com.nikichxp.tgbot.auth

import org.springframework.data.annotation.Id
import org.springframework.stereotype.Service

@Service
class AuthService {



}

data class RefreshToken(@Id val refreshToken: String)

data class AccessToken(val accessToken: String, val refreshToken: String)

interface AuthReason {

}