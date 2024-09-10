package com.nikichxp.tgbot.core.error

open class ExpectedError : Exception() {
    open val printJson = false
}

class DuplicatedRatingError : ExpectedError()
class NotHandledSituationError : ExpectedError() {
    override val printJson = true
}