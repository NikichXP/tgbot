package com.nikichxp.tgbot.handlers

import com.nikichxp.tgbot.core.CurrentUpdateProvider
import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.UpdateMarker
import com.nikichxp.tgbot.error.NotHandledSituationError
import com.nikichxp.tgbot.service.TgOperations
import com.nikichxp.tgbot.util.UserFormatter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import javax.annotation.PostConstruct

/*
TODO
  данный замечательный восхитительный класс пока что работает держа все чаты в оперативке
  пока чатов менее 100 это прокатит
  когда чатов будет больше 100 нужно будет добавлять сюда redis
  хранить данные там типа date.chatId.userId=0
  хранить информацию об имени в БД в течении пары дней и обновлять ее как-нибудь
 */
@Component
class MessageStatHandler(
    private val mongoTemplate: MongoTemplate,
    private val currentUpdateProvider: CurrentUpdateProvider,
    private val tgOperations: TgOperations
) : UpdateHandler {

    private lateinit var userStat: UserStat

//    @PostConstruct
    fun init() {
        this.userStat = mongoTemplate.findById(getDateKey()) ?: UserStat()
        mongoTemplate.find<UserStat>(Query.query(Criteria.where("hasReport").`is`(false)))
            .filter { it.date != getDateKey() }
            .forEach { reportInChat(it) }
    }

//    @Scheduled(fixedDelay = 1000 * 10)
    fun saveData() {
        mongoTemplate.save(userStat)
        if (getDateKey() != userStat.date) {
            reportInChat(userStat)
            userStat = UserStat()
        }
    }

    // TODO fix codestyle; refactor
    private fun reportInChat(stats: UserStat) {
        for (chatId in stats.getChatIds()) {
            val message = StringBuilder()
            message.append("Statistics for ${stats.date}:\n")
            var report = false
            for ((userId, count) in stats.getStatsForChat(chatId).toList().sortedByDescending { it.second }) {
                report = true
                message.append("${stats.userNames[userId]} - $count messages\n")
            }
            if (report) {
                tgOperations.sendMessage(chatId.toString(), message.toString())
            }
        }
        mongoTemplate.updateFirst(
            Query.query(Criteria.where("_id").`is`(stats.date)),
            org.springframework.data.mongodb.core.query.Update.update("hasReport", true),
            UserStat::class.java
        )
    }

    override fun getMarkers(): Set<UpdateMarker> {
        return setOf(UpdateMarker.MESSAGE_IN_GROUP)
    }

    override fun handleUpdate(update: Update) {
        val (userId, userName) = getIdAndName(update)
        val chatId = currentUpdateProvider.update?.message?.chat?.id ?: throw NotHandledSituationError()
        userStat.processNewMessage(chatId, userId, userName)
    }

    private fun getIdAndName(update: Update): Pair<Long, String> {
        val user = update.message?.from ?: throw NotHandledSituationError()
        return user.id to UserFormatter.getUserPrintName(user)
    }
}

fun getDateKey(date: LocalDate = LocalDateTime.now().toLocalDate()) = date.toString()

class UserStat {
    @Id
    var date = getDateKey()
    var userToCountMap = mutableMapOf<Long, MutableMap<Long, Int>>()
    var userNames = mutableMapOf<Long, String>()
    var hasReport = false

    fun processNewMessage(chatId: Long, userId: Long, userName: String) {
        val map = userToCountMap[chatId] ?: mutableMapOf<Long, Int>().also { userToCountMap[chatId] = it }
        map[userId] = (map[userId] ?: 0) + 1
        userNames[userId] = userName
    }

    fun getChatIds() = userToCountMap.keys

    /**
     * Get statistics for chat: userId to count of messages
     */
    fun getStatsForChat(chatId: Long): Map<Long, Int> =
        userToCountMap[chatId] ?: mapOf()

}
