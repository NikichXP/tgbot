package com.nikichxp.tgbot

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nikichxp.tgbot.core.config.ApplicationBeans
import com.nikichxp.tgbot.core.service.tgapi.TgSentMessageResponse
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.io.Resource

@SpringBootTest(classes = [ApplicationBeans::class])
@Import(ApplicationBeans::class)
class TgResponseParserTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Value("classpath:responses/sentMessageResponse.json")
    lateinit var resourceFile: Resource

    @Test
    fun mapsResponse() {
        val response = objectMapper.readValue<TgSentMessageResponse>(resourceFile.file)

        assertThat(response).isNotNull()
        assertThat(response.result?.messageId).isNotNull().isNotEqualTo(0L)
        assertThat(response.result?.chat?.id).isNotNull().isNotEqualTo(0L)
    }

}
