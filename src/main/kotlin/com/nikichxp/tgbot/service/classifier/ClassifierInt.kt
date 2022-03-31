package com.nikichxp.tgbot.service.classifier

import org.springframework.stereotype.Indexed

@Indexed
interface ClassifierInt {

    fun classify(input: String): Double

}