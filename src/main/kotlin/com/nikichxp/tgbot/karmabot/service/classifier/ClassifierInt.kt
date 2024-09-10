package com.nikichxp.tgbot.karmabot.service.classifier

import org.springframework.stereotype.Indexed

@Indexed
interface ClassifierInt {

    fun classify(input: String): Double

}