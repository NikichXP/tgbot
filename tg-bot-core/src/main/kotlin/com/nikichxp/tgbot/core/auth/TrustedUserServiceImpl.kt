package com.nikichxp.tgbot.core.auth

import com.nikichxp.tgbot.core.config.AppConfig
import com.nikichxp.tgbot.core.entity.UpdateContext
import org.springframework.stereotype.Service

// TODO Migrate that to user lists
@Service
class TrustedUserServiceImpl(
    private val appConfig: AppConfig
) : ITrustedUserService {

    private data class ParsedEntries(
        val ids: Set<Long>,
        val usernames: Set<String>
    )

    private val parsed: ParsedEntries by lazy { parse(appConfig.trustedUsers) }

    override fun isTrusted(userId: Long?, username: String?): Boolean {
        if (isAdmin(userId)) return true
        if (userId != null && userId in parsed.ids) return true
        val normalized = username?.removePrefix("@")?.lowercase()
        return normalized != null && normalized in parsed.usernames
    }

    override fun isTrusted(context: UpdateContext): Boolean {
        return isTrusted(context.from?.id, context.from?.username)
    }

    override fun isAdmin(userId: Long?): Boolean {
        return userId != null && appConfig.adminId != 0L && userId == appConfig.adminId
    }

    override fun isAdmin(context: UpdateContext): Boolean {
        return isAdmin(context.from?.id)
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
