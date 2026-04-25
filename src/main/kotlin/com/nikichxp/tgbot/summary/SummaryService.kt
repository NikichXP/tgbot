package com.nikichxp.tgbot.summary

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.util.AppStorage
import com.nikichxp.tgbot.core.util.getContextChatId
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class SummaryService(
    private val messageDAO: MessageDAO,
    private val appStorage: AppStorage,
    private val chatUpdatesToPromptSerializerService: ChatUpdatesToPromptSerializerService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Cacheable("summary_feature")
    fun getFeatureEnabledStatus(chatId: Long): Boolean {
        return appStorage.getData("summary_feature_$chatId")?.value?.toBoolean() ?: false
    }

    @CacheEvict("summary_feature", allEntries = true)
    fun setFeatureEnabledStatus(chatId: Long, enabled: Boolean) {
        appStorage.saveData("summary_feature_$chatId", enabled.toString())
    }

    fun saveUpdate(update: Update) {
        val chatId = update.getContextChatId()
        if (chatId == null) {
            logger.warn("No chat id in update $update")
            return
        }
        messageDAO.storeMessage(LoggedMessage(update = update, chatId = chatId))
    }

    fun getUpdatesForChat(chatId: Long): List<LoggedMessage> {
        return messageDAO.getMessages(chatId)
    }
}