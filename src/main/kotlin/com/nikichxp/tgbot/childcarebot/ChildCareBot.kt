package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.ChildActivity.EATING
import com.nikichxp.tgbot.childcarebot.ChildActivity.SLEEP
import com.nikichxp.tgbot.childcarebot.ChildActivity.WAKE_UP
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import org.bson.types.ObjectId
import org.springframework.context.annotation.Lazy
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query


@Service
class ChildCareCommandHandler(
    private val tgOperations: TgOperations,
    private val childActivityService: ChildActivityService,
    @Lazy private val appConfig: AppConfig
) : CommandHandler, UpdateHandler {

    private val buttonToActivityMap = mapOf(
        SLEEP to "Уснула",
        WAKE_UP to "Проснулась",
        EATING to "Кушает"
    )

    private val possibleTransitions = mapOf(
        SLEEP to setOf(WAKE_UP),
        WAKE_UP to setOf(EATING, SLEEP),
        EATING to setOf(SLEEP)
    )

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.CHILDTRACKERBOT)

    @HandleCommand("/status")
    suspend fun status() {
        val lastState = childActivityService.getLatestState()
        val buttons = possibleTransitions[lastState]?.map { buttonToActivityMap[it] ?: "SNF: $it" } ?: listOf("ERROR")

        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Active state: ${buttonToActivityMap[lastState]}"
            withKeyboard(listOf(buttons))
        }
    }

    override fun botSupported(bot: TgBot): Boolean = bot == TgBot.CHILDTRACKERBOT

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_CHAT)

    override suspend fun handleUpdate(update: Update) {

        if (update.getContextChatId() != appConfig.adminId) {
            tgOperations.replyToCurrentMessage("${update.getContextChatId()} != ${appConfig.adminId}")
            tgOperations.replyToCurrentMessage("You are not allowed to use this bot ~_~")
            return
        }

        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "handling update!" + update.getContextChatId()
        }
    }
}


enum class ChildActivity {
    SLEEP, WAKE_UP, EATING
}

data class ChildActivityEvent(
    val activity: ChildActivity,
    val date: LocalDateTime
) {
    lateinit var id: ObjectId
}

@Service
class ChildActivityService(
    private val mongoTemplate: MongoTemplate
) {

    fun addActivity(activity: ChildActivity) {
        val event = ChildActivityEvent(activity, LocalDateTime.now())
        mongoTemplate.save(event)
    }

    fun getActivities(): List<ChildActivityEvent> {
        return mongoTemplate.findAll()
    }

    fun getLatestState(): ChildActivity {
        val lastActivity = mongoTemplate.findOne<ChildActivityEvent>(
            Query().with(Sort.by(Sort.Order.desc(ChildActivityEvent::date.name))).limit(1)
        )
        return lastActivity?.activity ?: WAKE_UP
    }

}