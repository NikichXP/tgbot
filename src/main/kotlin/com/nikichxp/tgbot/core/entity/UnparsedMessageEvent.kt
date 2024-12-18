package com.nikichxp.tgbot.core.entity

import org.springframework.context.ApplicationEvent

class UnparsedMessageEvent(source: Any, val unparsedMessage: UnparsedMessage) : ApplicationEvent(source)