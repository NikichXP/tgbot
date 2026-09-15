package com.nikichxp.tgbot.core.error

open class ExpectedError : Exception() {
    open val printJson = false
}

open class DisplayableError(val displayedMessage: String): ExpectedError()

class DuplicatedRatingError : ExpectedError()
class NotHandledSituationError : ExpectedError() {
    override val printJson = true
}