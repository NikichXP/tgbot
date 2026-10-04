package com.nikichxp.tgbot.dashboard.converters

import com.nikichxp.tgbot.core.handlers.BotFeature
import com.nikichxp.tgbot.dashboard.dto.DashboardFeatureDto
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class BotFeatureToDashboardFeatureDtoConverter : Converter<BotFeature, DashboardFeatureDto> {

    override fun convert(source: BotFeature) = DashboardFeatureDto(
        id = source.id,
        title = source.title,
        description = source.description
    )
}
