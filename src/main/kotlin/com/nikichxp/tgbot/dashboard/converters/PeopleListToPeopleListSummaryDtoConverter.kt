package com.nikichxp.tgbot.dashboard.converters

import com.nikichxp.tgbot.dashboard.dto.PeopleListSummaryDto
import com.nikichxp.tgbot.people.entity.PeopleList
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class PeopleListToPeopleListSummaryDtoConverter : Converter<PeopleList, PeopleListSummaryDto> {

    override fun convert(source: PeopleList) = PeopleListSummaryDto(
        name = source.name,
        description = source.description,
        memberCount = source.userIds.size,
        updatedAt = source.updatedAt.toString()
    )
}
