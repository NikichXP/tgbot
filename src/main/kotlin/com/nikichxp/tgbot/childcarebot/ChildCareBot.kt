package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.ChildActivity.EATING
import com.nikichxp.tgbot.childcarebot.ChildActivity.SLEEP
import com.nikichxp.tgbot.childcarebot.ChildActivity.WAKE_UP
import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.entity.UpdateMarker
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.UpdateHandler
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextChatId
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ChildCareCommandHandler(
    private val tgOperations: TgOperations,
    private val childActivityService: ChildActivityService,
    private val appConfig: AppConfig
) : CommandHandler, UpdateHandler, Authenticable {

    private val buttonStateMap = mapOf(
        SLEEP to "Уснула",
        WAKE_UP to "Проснулась",
        EATING to "Кушает"
    )

    private val stateButtonMap = buttonStateMap.map { it.value to it.key }.toMap()

    private val possibleTransitions = mapOf(
        SLEEP to setOf(WAKE_UP),
        WAKE_UP to setOf(EATING, SLEEP),
        EATING to setOf(SLEEP)
    )

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.CHILDTRACKERBOT)

    override suspend fun authenticate(update: Update): Boolean {
        if (update.getContextChatId() != appConfig.adminId + 1) {
            tgOperations.replyToCurrentMessage("You are not allowed to use this bot ~_~")
            return false
        }
        return true
    }

    @HandleCommand("/status")
    suspend fun status(update: Update) {

        if (update.getContextChatId() != appConfig.adminId) {
            tgOperations.replyToCurrentMessage("You are not allowed to use this bot ~_~")
            return
        }

        val lastState = childActivityService.getLatestState()
        val buttons = getButtonsForState(lastState)

        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Active state: ${buttonStateMap[lastState]}"
            withKeyboard(listOf(buttons))
        }
    }

    @HandleCommand("/report")
    suspend fun report(update: Update) {
        if (update.getContextChatId() != appConfig.adminId) {
            tgOperations.replyToCurrentMessage("You are not allowed to use this bot ~_~")
            return
        }

        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = childActivityService.getActivities().joinToString("\n") { "${it.activity} at ${it.date}" }
        }
    }

    private fun getButtonsForState(state: ChildActivity): List<String> {
        return possibleTransitions[state]?.map { buttonStateMap[it] ?: "SNF: $it" } ?: listOf("ERROR")
    }

    override fun botSupported(bot: TgBot): Boolean = bot == TgBot.CHILDTRACKERBOT

    override fun getMarkers() = setOf(UpdateMarker.MESSAGE_IN_CHAT, UpdateMarker.IS_NOT_COMMAND)

    override suspend fun handleUpdate(update: Update) {
        val command = update.message?.text

        if (command == null) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "No command found"
            }
            return
        }

        val currentState = childActivityService.getLatestState()
        val possibleCommandStates = possibleTransitions[currentState]?.map(buttonStateMap::get) ?: emptyList()

        if (command in possibleCommandStates) {
            val newState = stateButtonMap[command] ?: return
            childActivityService.addActivity(newState)
            tgOperations.sendMessage {
                text = "State changed to $command"
                replyToCurrentMessage()
                withKeyboard(listOf(getButtonsForState(newState)))
            }
        } else {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "not yet implemented"
            }
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