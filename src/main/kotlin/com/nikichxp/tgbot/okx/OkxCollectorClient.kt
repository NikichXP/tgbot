package com.nikichxp.tgbot.okx

import com.nikichxp.tgbot.core.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.springframework.stereotype.Service

@Service
class OkxCollectorClient(
    private val httpClient: HttpClient,
    private val appConfig: AppConfig,
) {

    suspend fun prices(): List<OkxPrice> =
        httpClient.get("${appConfig.okx.baseUrl.trimEnd('/')}/prices").body()
}
