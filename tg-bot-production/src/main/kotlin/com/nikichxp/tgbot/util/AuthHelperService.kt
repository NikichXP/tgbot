package com.nikichxp.tgbot.util

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.UserId
import com.nikichxp.tgbot.core.error.PermissionDeniedError
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import org.springframework.stereotype.Service

@Service
class AuthHelperService(
    private val tgMessageService: ITgMessageService,
    private val appConfig: AppConfig
) {

    suspend fun checkActionDoneByAdmin(updateContext: UpdateContext) {
        checkActionDoneByUserMatching(updateContext) { userId -> userId != appConfig.adminId }
    }

    suspend fun checkActionDoneByOwner(updateContext: UpdateContext) {
        // so far owner is the only admin :)
        checkActionDoneByUserMatching(updateContext) { userId -> userId != appConfig.adminId }
    }

    suspend fun checkActionDoneByUserMatching(updateContext: UpdateContext, userIdPredicate: suspend (UserId) -> Boolean) {
        val callerId = updateContext.from?.id ?: throw IllegalArgumentException("Can't get userId")

        if (!userIdPredicate(callerId)) {
            accessDenied()
        }
    }

    private suspend fun accessDenied() {
        throw PermissionDeniedError("You are not allowed to use this command")
    }

}