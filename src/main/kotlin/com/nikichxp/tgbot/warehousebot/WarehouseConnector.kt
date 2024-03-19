package com.nikichxp.tgbot.warehousebot

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class WarehouseConnector(
    private val client: HttpClient
) {

    @Value("\${app.warehouse.url}")
    private lateinit var serviceUrl: String

    suspend fun listWarehouseEntities(userId: String): List<SKU> {
        val response = client
            .get {
                url("$serviceUrl/list")
                header("user", userId)
            }
        return response.body()
    }

    suspend fun getWarehouseEntity(userId: String, id: String): SKU {
        val response = client
            .get {
                url("$serviceUrl/$id")
                header("user", userId)
            }
        return response.body()
    }

    suspend fun createWarehouseEntity(userId: String, request: SKUCreateRequest): SKU {
        val response = client
            .post {
                url("$serviceUrl/")
                header("user", userId)
                setBody(request)
            }
        return response.body()
    }

    suspend fun updateWarehouseEntity(userId: String, id: String, quantity: Int): Boolean {
        val response = client
            .put {
                url("$serviceUrl/$id")
                header("user", userId)
                setBody(quantity)
            }
        return response.status.value in 200..299
    }

    suspend fun deleteWarehouseEntity(userId: String, id: String): Boolean {
        val response = client
            .delete {
                url("$serviceUrl/$id")
                header("user", userId)
            }
        return response.status.value in 200..299
    }
}

data class SKUCreateRequest(
    val name: String,
    val quantity: Int,
    val tags: Set<String> = setOf()
)

data class SKU(
    val id: String,
    var name: String,
    var quantity: Int = 0,
    var tags: Set<String> = setOf()
)