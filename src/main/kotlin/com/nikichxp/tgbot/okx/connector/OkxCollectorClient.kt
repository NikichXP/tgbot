package com.nikichxp.tgbot.okx.connector

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.okx.dto.OkxPrice
import com.nikichxp.tgbot.okx.error.OkxCollectorException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import org.springframework.stereotype.Service

@Service
class OkxCollectorClient(
    private val httpClient: HttpClient,
    private val appConfig: AppConfig,
) {

    suspend fun prices(): List<OkxPrice> {
        val response = httpClient.get("${appConfig.okx.baseUrl.trimEnd('/')}/prices")
            throw OkxCollectorException("okx-collector GET /prices returned HTTP ${response.status.value}")
        }
        return response.body()
    }
}
