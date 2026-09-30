package com.nikichxp.tgbot.dashboard.dto

import java.time.Duration

data class IssuedTokens(val accessToken: String, val refreshToken: String, val expiresIn: Duration)
