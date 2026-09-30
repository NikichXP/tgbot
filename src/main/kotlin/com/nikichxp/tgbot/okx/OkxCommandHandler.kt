package com.nikichxp.tgbot.okx

import com.nikichxp.tgbot.core.entity.UpdateContext
import com.nikichxp.tgbot.core.entity.bots.TgBotInfo
import com.nikichxp.tgbot.core.handlers.Features
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.TgMessageService
import com.nikichxp.tgbot.util.AuthHelperService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class OkxCommandHandler(
    private val tgMessageService: TgMessageService,
    private val okxCollectorClient: OkxCollectorClient,
    private val rabbitTemplate: RabbitTemplate,
    private val authHelperService: AuthHelperService
) : CommandHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun requiredFeatures() = setOf(Features.OKX)

    @HandleCommand("/prices")
    suspend fun prices(): Boolean {
        val text = try {
            val prices = okxCollectorClient.prices()
            if (prices.isEmpty()) {
                "Нет отслеживаемых тикеров"
            } else {
                prices.joinToString("\n") { "${it.instrumentId}: ${it.price?.let(::formatNumber) ?: "—"}" }
            }
        } catch (e: Exception) {
            logger.warn("Failed to fetch prices from okx-collector", e)
            "Не удалось получить цены: ${e.message}"
        }
        tgMessageService.replyToCurrentMessage(text)
        return true
    }

    @HandleCommand("/watch")
    suspend fun watch(args: List<String>, updateContext: UpdateContext): Boolean {
        authHelperService.checkActionDoneByOwner(updateContext)

        val threshold = args.getOrNull(1)?.let(Threshold::parse)
        if (args.size != 2 || threshold == null) {
            tgMessageService.replyToCurrentMessage(
                "Использование: /watch <тикер> <порог>\n" +
                    "Например: /watch BTC-USDT-SWAP 2% или /watch BTC 500"
            )
            return true
        }
        send(updateContext, WatchAction.WATCH, args[0], threshold)
        return true
    }

    @HandleCommand("/unwatch")
    suspend fun unwatch(args: List<String>, updateContext: UpdateContext): Boolean {
        authHelperService.checkActionDoneByOwner(updateContext)

        if (args.size != 1) {
            tgMessageService.replyToCurrentMessage("Использование: /unwatch <тикер>")
            return true
        }

        send(updateContext, WatchAction.UNWATCH, args[0])
        return true
    }

    @HandleCommand("/watches")
    suspend fun watches(updateContext: UpdateContext): Boolean {
        send(updateContext, WatchAction.LIST)
        return true
    }

    private suspend fun send(
        updateContext: UpdateContext,
        action: WatchAction,
        instrumentId: String? = null,
        threshold: Threshold? = null,
    ) {
        val bot = updateContext.getBotInfo() as? TgBotInfo ?: return
        val command = WatchCommand(
            action = action,
            chatId = updateContext.getChatId(),
            botName = bot.name,
            instrumentId = instrumentId,
            thresholdType = threshold?.type,
            thresholdValue = threshold?.value,
        )
        try {
            rabbitTemplate.convertAndSend(OKX_WATCH_COMMANDS_QUEUE, command)
        } catch (e: Exception) {
            logger.error("Failed to send watch command {}", command, e)
            tgMessageService.replyToCurrentMessage("Не удалось отправить команду: ${e.message}")
        }
    }
}
