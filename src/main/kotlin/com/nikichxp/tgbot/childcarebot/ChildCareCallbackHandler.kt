package com.nikichxp.tgbot.childcarebot

import com.nikichxp.tgbot.childcarebot.logic.ChildInfoRepo
import com.nikichxp.tgbot.childcarebot.logic.ChildReportHelper
import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.bots.TgBot
import com.nikichxp.tgbot.core.handlers.Authenticable
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackContext
import com.nikichxp.tgbot.core.handlers.callbacks.CallbackHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.getContextUserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChildCareCallbackHandler(
    private val tgOperations: TgOperations,
    private val childInfoRepo: ChildInfoRepo,
    private val childReportHelper: ChildReportHelper,
) : Authenticable, CallbackHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun authenticate(update: Update): Boolean {
        val child = childInfoRepo.findChildByParent(update.getContextUserId()!!)

        if (child == null) {
            logger.warn("No user found for child: user id = ${update.getContextUserId()}")
            return false
        }

        return true
    }

    override fun requiredFeatures() = setOf(Features.CHILD_TRACKER)

    override fun supportedBots(): Set<TgBot> = setOf(TgBot.CHILDTRACKERBOT)

    override fun isCallbackSupported(callbackContext: CallbackContext): Boolean =
        callbackContext.bot == TgBot.CHILDTRACKERBOT

    override suspend fun handleCallback(
        callbackContext: CallbackContext,
        update: Update,
    ): Boolean {

        val data = callbackContext.data

        when {
            data == "sleep-schedule" -> childReportHelper.sleepReport(callbackContext)
            data == "feeding-schedule" -> childReportHelper.feedingReport(callbackContext)
            data.startsWith("minus-") -> {
                tgOperations.sendMessage {
                    replyToCurrentMessage()
                    text = "Minus minutes to sleep"
                }
            }

            data.startsWith("plus-") -> {
                tgOperations.sendMessage {
                    replyToCurrentMessage()
                    text = "Plus minutes to sleep"
                }
            }

            else -> {
                tgOperations.sendMessage {
                    replyToCurrentMessage()
                    text = "Unknown callback $data"
                }
            }
        }

        return true
    }
}