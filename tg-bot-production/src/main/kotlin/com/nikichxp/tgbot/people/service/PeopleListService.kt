package com.nikichxp.tgbot.people.service

import com.nikichxp.tgbot.people.entity.PeopleList
import com.nikichxp.tgbot.people.error.PeopleListAlreadyExistsException
import com.nikichxp.tgbot.people.error.PeopleListNotFoundException
import com.nikichxp.tgbot.people.repository.PeopleListRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PeopleListService(
    private val peopleListRepository: PeopleListRepository
) {

    private val listNameRegex = Regex("^[a-z0-9_-]{1,64}$")

    suspend fun isMember(listName: String, userId: Long): Boolean = peopleListRepository.isMember(listName, userId)

    suspend fun findAll(): List<PeopleList> = peopleListRepository.findAll()

    suspend fun get(name: String): PeopleList =
        peopleListRepository.findByName(name) ?: throw PeopleListNotFoundException(name)

    suspend fun create(name: String, description: String?): PeopleList {
        require(listNameRegex.matches(name)) { "List name may contain only a-z, 0-9, '_' and '-'" }
        val now = Instant.now()
        val list = PeopleList(name, description?.trim()?.ifEmpty { null }, emptySet(), now, now)
        if (!peopleListRepository.insertIfAbsent(list)) throw PeopleListAlreadyExistsException(name)
        return list
    }

    suspend fun updateDescription(name: String, description: String?): PeopleList {
        ensureUpdated(name, peopleListRepository.updateDescription(name, description?.trim()?.ifEmpty { null }, Instant.now()))
        return get(name)
    }

    suspend fun addMember(name: String, userId: Long): PeopleList {
        ensureUpdated(name, peopleListRepository.addMember(name, userId, Instant.now()))
        return get(name)
    }

    suspend fun removeMember(name: String, userId: Long): PeopleList {
        ensureUpdated(name, peopleListRepository.removeMember(name, userId, Instant.now()))
        return get(name)
    }

    suspend fun delete(name: String) {
        ensureUpdated(name, peopleListRepository.delete(name))
    }

    private fun ensureUpdated(name: String, updated: Boolean) {
        if (!updated) throw PeopleListNotFoundException(name)
    }
}
