package com.nikichxp.tgbot.childcarebot.state

import com.nikichxp.tgbot.childcarebot.ChildActivity
import com.nikichxp.tgbot.childcarebot.TgMessageInfo
import com.nikichxp.tgbot.core.dto.MessageId
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Indexed
import org.springframework.stereotype.Service
import java.util.UUID

@Indexed
interface StateTransitionHandler {

    fun from(): Set<ChildActivity>
    fun to(): Set<ChildActivity>

    fun doTransition(from: ChildActivity, to: ChildActivity, childId: Long)

}

@Service
class AddTimeNavigationHandler(
    private val mongoTemplate: MongoTemplate,
) : StateTransitionHandler {

    override fun from() = setOf(ChildActivity.WAKE_UP)

    override fun to() = setOf(ChildActivity.SLEEP)

    override fun doTransition(from: ChildActivity, to: ChildActivity, childId: Long) {

        TODO("Not yet implemented")
    }

}

class CallbackListen(var messageInfo: TgMessageInfo) {
    lateinit var id: ObjectId
    var callbackQuery = UUID.randomUUID().toString()
}