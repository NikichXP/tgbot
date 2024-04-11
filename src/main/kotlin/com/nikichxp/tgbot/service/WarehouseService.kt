package com.nikichxp.tgbot.service

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.warehousebot.WarehouseConnector
import org.springframework.stereotype.Service

@Service
class WarehouseService(
    private val warehouseConnector: WarehouseConnector
) {

    suspend fun list(update: Update): List<String> {
        val userId = update.message?.from?.id?.toString() ?: throw IllegalArgumentException("No user id")
        val list = warehouseConnector.listWarehouseEntities(userId)
        return list.map { "${it.name} id=${it.id} [${it.quantity}]" }
    }

    suspend fun get(update: Update, skuId: String): List<String> {
        val userId = update.message?.from?.id?.toString() ?: throw IllegalArgumentException("No user id")
        val entity = warehouseConnector.getWarehouseEntity(userId, skuId)
        return listOf("${entity.name} [${entity.quantity}]")
    }
}