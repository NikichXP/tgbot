package com.nikichxp.tgbot.core.auth

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.util.getContextUserId
import com.nikichxp.tgbot.core.util.getContextUserName
import org.springframework.stereotype.Service

@Service
class TrustedUserService(
    private val appConfig: AppConfig
) {

    private data class ParsedEntries(
        val ids: Set<Long>,
        val usernames: Set<String>
    )

    private val parsed: ParsedEntries by lazy { parse(appConfig.trustedUsers) }

    fun isTrusted(userId: Long?, username: String?): Boolean {
        if (userId != null && userId == appConfig.adminId) return true
        if (userId != null && userId in parsed.ids) return true
        val normalized = username?.removePrefix("@")?.lowercase()
        return normalized != null && normalized in parsed.usernames
    }

    fun isTrusted(update: Update): Boolean {
        return isTrusted(update.getContextUserId(), update.getContextUserName())
    }

    private fun parse(entries: List<String>): ParsedEntries {
        val ids = mutableSetOf<Long>()
        val usernames = mutableSetOf<String>()
        for (raw in entries) {
            val e = raw.trim()
            if (e.isEmpty()) continue
            val asId = e.toLongOrNull()
            if (asId != null) {
                ids += asId
            } else {
                usernames += e.removePrefix("@").lowercase()
            }
        }
        return ParsedEntries(ids, usernames)
    }
}
