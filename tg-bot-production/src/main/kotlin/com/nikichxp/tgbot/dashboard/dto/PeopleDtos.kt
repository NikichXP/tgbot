package com.nikichxp.tgbot.dashboard.dto

data class KnownUserDto(
    val id: Long,
    val username: String?,
    val fullName: String,
    val languageCode: String?,
    val isPremium: Boolean?,
    val firstSeenAt: String,
    val lastSeenAt: String,
    val bots: List<String>,
    val chats: List<KnownUserChatDto>
)

data class KnownUserChatDto(
    val chatId: Long,
    val title: String,
    val type: String,
    val lastSeenAt: String
)

data class PeopleListSummaryDto(
    val name: String,
    val description: String?,
    val memberCount: Int,
    val updatedAt: String
)

data class PeopleListDto(
    val name: String,
    val description: String?,
    val members: List<PeopleListMemberDto>,
    val createdAt: String,
    val updatedAt: String
)

data class PeopleListMemberDto(val id: Long, val user: KnownUserDto?)

data class CreatePeopleListRequest(val name: String, val description: String? = null)

data class UpdatePeopleListRequest(val description: String? = null)
