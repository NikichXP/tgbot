package com.nikichxp.tgbot.santabot

import com.nikichxp.bot.common.entity.UpdateContext
import com.nikichxp.bot.common.util.UserFormatter
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.handlers.commands.HandleCommand
import com.nikichxp.tgbot.core.service.tgapi.ITgMessageService
import com.nikichxp.tgbot.santabot.entity.SecretSantaGame
import com.nikichxp.tgbot.santabot.entity.SecretSantaPlayer
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service

@Service
class SantaBotCommandHandler(
    private val mongoTemplate: MongoTemplate,
    private val tgMessageService: ITgMessageService,
) : CommandHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun requiredFeatures() = setOf(SantaFeature)

    @HandleCommand("/create")
    private suspend fun commandCreate(args: List<String>, context: UpdateContext): Boolean {
        val game = SecretSantaGame()
        game.createdBy = context.from?.id ?: throw IllegalArgumentException("Can't get user id")

        // TODO argsparser
        var i = 0
        while (i < args.size) {
            val arg = args[i]
            when (arg) {
                "id", "-id" -> {
                    game.id = args[i + 1]
                    i++
                }
            }
            i++
        }

        val player = getSantaUserPlayerFromUpdate(context)
        game.players += player
        mongoTemplate.save(game)
        tgMessageService.replyToCurrentMessage("game created")
        return true
    }

    @HandleCommand("/register")
    private suspend fun commandRegister(args: List<String>, context: UpdateContext): Boolean {
        val gameId = args.first()
        val game = getGame(gameId) ?: return noGameFound()

        if (game.isStarted) {
            tgMessageService.replyToCurrentMessage("Игра уже начата")
            return true
        }

        val player = getSantaUserPlayerFromUpdate(context)
        var status = false
        if (game.players.none { it.id == player.id }) {
            game.players += player
            status = true
        }
        mongoTemplate.save(game)
        tgMessageService.replyToCurrentMessage(
            if (status) "Вы зарегистрировались в игре \"$gameId\"" else "Вы уже зарегистрированы в игре"
        )
        return true
    }

    @HandleCommand("/players")
    private suspend fun commandPlayers(args: List<String>, context: UpdateContext): Boolean {
        if (args.size != 1) {
            tgMessageService.replyToCurrentMessage("Используйте /players gameId")
            return true
        }

        val gameId = args.first()
        val game = getGame(gameId) ?: return noGameFound()

        if (game.players.none { it.id == context.from?.id }) {
            tgMessageService.replyToCurrentMessage("Вы не зарегистрированы в этой игре")
            return true
        }

        val players = game.players.joinToString("\n") { UserFormatter.mention(it.user) }
        tgMessageService.replyToCurrentMessage("Игроки в игре \"$gameId\":\n$players")
        return true
    }

    @HandleCommand("/ignore")
    private suspend fun commandIgnore(args: List<String>, context: UpdateContext): Boolean {
        if (args.size != 2) {
            tgMessageService.replyToCurrentMessage("Используйте /ignore gameId @username")
            return true
        }

        val gameId = args[0]
        val ignoredUsername = args[1].removePrefix("@").lowercase()
        val game = getGame(gameId) ?: return noGameFound()
        val playerId = getSantaUserPlayerFromUpdate(context).id
        val player = game.players.find { it.id == playerId }
            ?: throw IllegalArgumentException("register in game first")
        player.ignoredUsernames += ignoredUsername
        mongoTemplate.save(game)
        tgMessageService.replyToCurrentMessage(
            "Вы добавили @$ignoredUsername как свою вторую половинку! " +
                    "Теперь вы не будете дарить ему подарок, а он не будет дарить подарок вам."
        )
        return true
    }

    @HandleCommand("/testgame")
    private suspend fun commandTestGame(args: List<String>, context: UpdateContext): Boolean {
        val gameId = args.first()
        val game = getGame(gameId) ?: return noGameFound()

        if (game.createdBy != context.from?.id) {
            tgMessageService.replyToCurrentMessage("Вы не создатель игры")
            return true
        }

        val playerPairs = findPairs(game) ?: return true
        tgMessageService.replyToCurrentMessage(
            playerPairs.joinToString("\n") { (santa, target) ->
                "${UserFormatter.mention(santa.user)} -> ${UserFormatter.mention(target.user)}"
            }
        )
        return true
    }

    @HandleCommand("/startgame")
    private suspend fun commandStartGame(args: List<String>, context: UpdateContext): Boolean {
        val gameId = args.first()
        val game = getGame(gameId) ?: return noGameFound()

        when {
            game.isStarted -> tgMessageService.replyToCurrentMessage("Игра уже начата")
            game.players.size < 3 -> tgMessageService.replyToCurrentMessage("Недостаточно игроков")
            game.createdBy != context.from?.id -> tgMessageService.replyToCurrentMessage("Вы не создатель игры")
            else -> {
                val playerPairs = findPairs(game) ?: return true
                startGame(playerPairs)
                game.isStarted = true
                mongoTemplate.save(game)
            }
        }
        return true
    }

    private suspend fun getGame(gameId: String): SecretSantaGame? {
        return mongoTemplate.findById<SecretSantaGame>(gameId)
    }

    private suspend fun noGameFound(): Boolean {
        tgMessageService.replyToCurrentMessage("Игра не найдена")
        return true
    }

    private suspend fun findPairs(game: SecretSantaGame): List<Pair<SecretSantaPlayer, SecretSantaPlayer>>? {
        val pairs = SecretSantaPairing.findPairs(game.players)
        if (pairs == null) {
            tgMessageService.replyToCurrentMessage("Не удалось найти подходящие пары. Попробуйте еще раз")
        }
        return pairs
    }

    private suspend fun startGame(playerPairs: List<Pair<SecretSantaPlayer, SecretSantaPlayer>>) {
        playerPairs.forEach { (santa, target) ->
            log.info("${UserFormatter.diagnostic(santa.user, "santa")} дарит ${UserFormatter.diagnostic(target.user, "target")}")
            tgMessageService.sendMessage(
                santa.id,
                "Ваша цель: ${UserFormatter.mention(target.user)}"
            )
        }
    }

    private fun getSantaUserPlayerFromUpdate(context: UpdateContext): SecretSantaPlayer {
        return SecretSantaPlayer(context.from ?: throw IllegalArgumentException("not expected"))
    }
}
