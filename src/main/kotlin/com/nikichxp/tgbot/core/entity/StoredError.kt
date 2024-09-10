package com.nikichxp.tgbot.core.entity

data class StoredError(
    var message: String,
    var data: Any
) {
    lateinit var id: String
}