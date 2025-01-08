package com.nikichxp.tgbot.core.service.tgapi

import com.fasterxml.jackson.annotation.JsonProperty

data class TgSentMessageResponse(
    val ok: Boolean,
    val result: TgResult?
)

/*
example response:
{
  "ok" : true,
  "result" : {
    "message_id" : 24643,
    "from" : {
      "id" : 360838312,
      "is_bot" : true,
      "first_name" : "Бот Никича",
      "username" : "nikichbot"
    },
    "chat" : {
      "id" : 34080460,
      "first_name" : "Никита",
      "username" : "NikichXP",
      "type" : "private"
    },
    "date" : 1736291493,
    "text" : "test"
  }
}
 */
class TgResult {
    @JsonProperty("message_id")
    var messageId: Long = 0
}

