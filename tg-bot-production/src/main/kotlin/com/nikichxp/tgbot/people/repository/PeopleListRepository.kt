package com.nikichxp.tgbot.people.repository

import com.nikichxp.tgbot.people.entity.PeopleList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.exists
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.remove
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PeopleListRepository(
    private val mongoTemplate: MongoTemplate
) {

    suspend fun findAll(): List<PeopleList> = withContext(Dispatchers.IO) {
        mongoTemplate.find<PeopleList>(Query().with(Sort.by(PeopleList::name.name)))
    }

    suspend fun findByName(name: String): PeopleList? = withContext(Dispatchers.IO) {
        mongoTemplate.findById<PeopleList>(name)
    }

    suspend fun insertIfAbsent(list: PeopleList): Boolean = withContext(Dispatchers.IO) {
        try {
            mongoTemplate.insert(list)
            true
        } catch (e: DuplicateKeyException) {
            false
        }
    }

    suspend fun updateDescription(name: String, description: String?, at: Instant): Boolean =
        updateExisting(name, Update().set(PeopleList::description.name, description), at)

    suspend fun addMember(name: String, userId: Long, at: Instant): Boolean =
        updateExisting(name, Update().addToSet(PeopleList::userIds.name, userId), at)

    suspend fun removeMember(name: String, userId: Long, at: Instant): Boolean =
        updateExisting(name, Update().pull(PeopleList::userIds.name, userId), at)

    suspend fun delete(name: String): Boolean = withContext(Dispatchers.IO) {
        mongoTemplate.remove<PeopleList>(byName(name)).deletedCount > 0
    }

    suspend fun isMember(name: String, userId: Long): Boolean = withContext(Dispatchers.IO) {
        mongoTemplate.exists<PeopleList>(Query(Criteria.where("_id").`is`(name).and(PeopleList::userIds.name).`is`(userId)))
    }

    private suspend fun updateExisting(name: String, update: Update, at: Instant): Boolean =
        withContext(Dispatchers.IO) {
            mongoTemplate.updateFirst(byName(name), update.set(PeopleList::updatedAt.name, at), PeopleList::class.java)
                .matchedCount > 0
        }

    private fun byName(name: String) = Query(Criteria.where("_id").`is`(name))
}
