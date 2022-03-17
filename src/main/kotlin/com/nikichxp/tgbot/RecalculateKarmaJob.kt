package com.nikichxp.tgbot

import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.service.actions.LikeReport
import com.nikichxp.tgbot.service.actions.LikedMessageService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

//@Component
class RecalculateKarmaJob(
    private val mongoTemplate: MongoTemplate,
    private val tgOperations: TgOperations
) {

//    @PostConstruct
    fun work() {
        val karmaPoints = mutableMapOf<Long, Double>()
        mongoTemplate.findAll<LikeReport>().forEach {
            val actorKarma = karmaPoints[it.id.authorId] ?: 0.0
            val targetKarma = karmaPoints[it.id.targetId] ?: 0.0
//            LikedMessageService.calculateKarmaDiff(actorKarma, )
            println(it)
        }
    }

}