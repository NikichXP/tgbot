package com.nikichxp.tgbot.people.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "peopleLists")
data class PeopleList(
    @Id val name: String,
    val description: String?,
    val userIds: Set<Long>,
    val createdAt: Instant,
    val updatedAt: Instant
)
