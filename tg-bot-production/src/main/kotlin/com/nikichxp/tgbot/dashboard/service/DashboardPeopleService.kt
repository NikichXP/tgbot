package com.nikichxp.tgbot.dashboard.service

import com.nikichxp.tgbot.dashboard.dto.KnownUserDto
import com.nikichxp.tgbot.dashboard.dto.PeopleListDto
import com.nikichxp.tgbot.dashboard.dto.PeopleListMemberDto
import com.nikichxp.tgbot.dashboard.dto.PeopleListSummaryDto
import com.nikichxp.tgbot.people.entity.KnownUser
import com.nikichxp.tgbot.people.entity.PeopleList
import com.nikichxp.tgbot.people.service.KnownUserService
import com.nikichxp.tgbot.people.service.PeopleListService
import com.nikichxp.tgbot.people.service.UserAvatarService
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service

@Service
class DashboardPeopleService(
    private val knownUserService: KnownUserService,
    private val peopleListService: PeopleListService,
    private val userAvatarService: UserAvatarService,
    private val conversionService: ConversionService
) {

    suspend fun searchPeople(text: String?, limit: Int): List<KnownUserDto> =
        knownUserService.search(text, limit.coerceIn(1, MAX_SEARCH_LIMIT)).map { it.toDto() }

    suspend fun avatar(userId: Long): ByteArray? = userAvatarService.getAvatar(userId)

    suspend fun lists(): List<PeopleListSummaryDto> =
        peopleListService.findAll().map { conversionService.convert(it, PeopleListSummaryDto::class.java)!! }

    suspend fun list(name: String): PeopleListDto = peopleListService.get(name).toDto()

    suspend fun createList(name: String, description: String?): PeopleListDto =
        peopleListService.create(name.trim(), description).toDto()

    suspend fun updateListDescription(name: String, description: String?): PeopleListDto =
        peopleListService.updateDescription(name, description).toDto()

    suspend fun addListMember(name: String, userId: Long): PeopleListDto =
        peopleListService.addMember(name, userId).toDto()

    suspend fun removeListMember(name: String, userId: Long): PeopleListDto =
        peopleListService.removeMember(name, userId).toDto()

    suspend fun deleteList(name: String) = peopleListService.delete(name)

    private suspend fun PeopleList.toDto(): PeopleListDto {
        val knownUsers = knownUserService.findAllById(userIds)
        val members = userIds
            .map { PeopleListMemberDto(it, knownUsers[it]?.toDto()) }
            .sortedBy { it.user?.fullName?.lowercase() ?: "￿${it.id}" }
        return PeopleListDto(name, description, members, createdAt.toString(), updatedAt.toString())
    }

    private fun KnownUser.toDto() = conversionService.convert(this, KnownUserDto::class.java)!!

    companion object {
        private const val MAX_SEARCH_LIMIT = 200
    }
}
