package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.entity.TgUpdateField
import com.nikichxp.tgbot.core.entity.TgUpdateFieldsEvent
import io.ktor.util.collections.ConcurrentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class TgUpdateFieldService(
    private val sendMessageToAdminService: SendMessageToAdminService,
    private val mongoTemplate: MongoTemplate,
    private val coroutineScope: CoroutineScope
) : ApplicationListener<TgUpdateFieldsEvent> {

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val existingFieldsCache = ConcurrentSet<String>()

    override fun onApplicationEvent(event: TgUpdateFieldsEvent) {
        event.paths.sorted().forEach { path ->
            if (existingFieldsCache.contains(path)) {
                return@forEach
            }

            synchronized(this) {
                val exists = mongoTemplate.exists<TgUpdateField>(
                    Query.query(Criteria.where(TgUpdateField::path.name).`is`(path))
                )
                if (!exists) {
                    try {
                        mongoTemplate.save(TgUpdateField(path = path, bot = event.bot))
                    } catch (e: DuplicateKeyException) {
                        return@forEach
                    } finally {
                        existingFieldsCache.add(path)
                    }
                    coroutineScope.launch {
                        sendMessageToAdminService.sendMessage("New update field: `$path` (bot: ${event.bot.name})")
                    }
                    log.info("New update field detected: {} (bot: {})", path, event.bot.name)
                } else {
                    existingFieldsCache.add(path)
                }
            }
        }
    }

}
