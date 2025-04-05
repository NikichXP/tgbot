package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildInfoRepo
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextUserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChildParentsCommandHandler(
    private val childInfoRepo: ChildInfoRepo,
    private val tgOperations: TgOperations
) : CommandHandler, Authenticable {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.CHILDTRACKERBOT)

    override suspend fun authenticate(update: Update): Boolean {
        val child = childInfoRepo.findChildByParent(update.getContextUserId()!!)

        if (child == null) {
            logger.warn("No user found for child: user id = ${update.getContextUserId()}")
            return false
        }

        return true
    }

    @HandleCommand("/addparent")
    suspend fun addParent(args: List<String>) {
        if (args.size != 2) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "Usage: /addparent <child_id> <parent_id>"
            }
            return
        }

        val childId = args[0].toLong()
        val parentId = args[1].toLong()

        childInfoRepo.updateById(childId) {
            it.parents += parentId
        }
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Parent added"
        }
    }

    @HandleCommand("/removeparent")
    suspend fun removeParent(args: List<String>) {
        if (args.size != 2) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "Usage: /removeparent <child_id> <parent_id>"
            }
            return
        }
        val childId = args[0].toLong()
        val parentId = args[1].toLong()

        childInfoRepo.updateById(childId) {
            it.parents -= parentId
        }
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Parent removed"
        }
    }

    @HandleCommand("/listparents")
    suspend fun listParents(args: List<String>) {
        if (args.size != 1) {
            tgOperations.sendMessage {
                replyToCurrentMessage()
                text = "Usage: /listparents <child_id>, got: ${args.joinToString(", ")}"
            }
            return
        }
        val childId = args[0].toLong()
        val child = childInfoRepo.findChildById(childId) ?: notFound(childId)
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = "Parents: ${child.parents.joinToString()}"
        }
    }

    private suspend fun <T> notFound(message: Any? = null): T {
        val errorMessage = "Child not found" + (message?.let { ": $it." } ?: ".")
        tgOperations.sendMessage {
            replyToCurrentMessage()
            text = errorMessage
        }
        throw IllegalArgumentException(errorMessage)
    }
}