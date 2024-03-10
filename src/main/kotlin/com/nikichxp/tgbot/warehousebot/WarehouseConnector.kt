package com.nikichxp.tgbot.warehousebot

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class WarehouseConnector(
    private val client: HttpClient
) {

    @PostConstruct
    fun init() {
        runBlocking {
            val test = getWarehouseEntities()
            println(test)
        }
    }

    suspend fun getWarehouseEntities(): List<SKU> {
        val response = client.get("https://warehouse.nikichxp.xyz/storage/list")
        return response.body()
    }

}

data class SKU(
    val id: String,
    var name: String,
    var quantity: Int = 0,
    var tags: Set<String> = setOf()
)