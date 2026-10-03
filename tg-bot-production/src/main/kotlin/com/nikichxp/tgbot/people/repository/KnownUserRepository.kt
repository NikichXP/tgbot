package com.nikichxp.tgbot.people.repository

import com.nikichxp.tgbot.people.dto.SeenUser
import com.nikichxp.tgbot.people.entity.KnownUser
import com.nikichxp.tgbot.people.entity.KnownUserChat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.regex.Pattern

@Repository
class KnownUserRepository(
    private val mongoTemplate: MongoTemplate
) {

    suspend fun upsertSeen(seen: SeenUser, at: Instant) {
        val user = seen.user
        val update = Update()
            .set(KnownUser::username.name, user.username)
            .set(KnownUser::fullName.name, user.fullName)
            .set(KnownUser::firstName.name, user.firstName)
            .set(KnownUser::lastName.name, user.lastName)
            .setIfPresent(KnownUser::languageCode.name, user.languageCode)
            .setIfPresent(KnownUser::isBot.name, user.isBot)
            .setIfPresent(KnownUser::isPremium.name, user.isPremium)
            .setOnInsert(KnownUser::firstSeenAt.name, at)
            .set(KnownUser::lastSeenAt.name, at)
            .addToSet(KnownUser::bots.name, seen.botName)
        seen.chat?.let {
            update.set("${KnownUser::chats.name}.${it.id}", KnownUserChat(it.id, it.title, it.type, at))
        }
        withContext(Dispatchers.IO) {
            mongoTemplate.upsert(byId(user.id), update, KnownUser::class.java)
        }
    }

    suspend fun search(text: String?, limit: Int): List<KnownUser> = withContext(Dispatchers.IO) {
        val query = Query().with(Sort.by(Sort.Direction.DESC, KnownUser::lastSeenAt.name)).limit(limit)
        text?.trim()?.removePrefix("@")?.takeIf { it.isNotEmpty() }?.let { query.addCriteria(matching(it)) }
        mongoTemplate.find<KnownUser>(query)
    }

    suspend fun findAllById(ids: Collection<Long>): List<KnownUser> = withContext(Dispatchers.IO) {
        mongoTemplate.find<KnownUser>(Query(Criteria.where("_id").`in`(ids)))
    }

    private fun matching(text: String): Criteria {
        val containsIgnoringCase = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE)
        val alternatives = mutableListOf(
            Criteria.where(KnownUser::username.name).regex(containsIgnoringCase),
            Criteria.where(KnownUser::fullName.name).regex(containsIgnoringCase)
        )
        text.toLongOrNull()?.let { alternatives += Criteria.where("_id").`is`(it) }
        return Criteria().orOperator(alternatives)
    }

    private fun Update.setIfPresent(field: String, value: Any?): Update = if (value != null) set(field, value) else this

    private fun byId(id: Long) = Query(Criteria.where("_id").`is`(id))
}
