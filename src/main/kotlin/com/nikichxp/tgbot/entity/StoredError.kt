package com.nikichxp.tgbot.entity

data class StoredError(
    var message: String,
    var data: Any
) {
    lateinit var id: String
}