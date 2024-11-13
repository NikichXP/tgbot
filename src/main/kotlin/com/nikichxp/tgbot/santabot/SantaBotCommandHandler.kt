package com.nikichxp.tgbot.santabot

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service
import java.util.*

@Service
class SantaBotCommandHandler(
    private val mongoTemplate: MongoTemplate,
    private val tgOperations: TgOperations
) : CommandHandler {

    private val rand = Random()
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun supportedBots(tgBot: TgBot) = setOf(TgBot.SANTABOT)

    override fun isCommandSupported(command: String): Boolean = true

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        when (command) {
            "/create" -> {
                val game = SecretSantaGame()

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

                val player = getSantaUserPlayerFromUpdate(update)
                game.players += player
                mongoTemplate.save(game)
                tgOperations.replyToCurrentMessage("game created")
            }

            "/register" -> {
                val gameId = args.first()
                val game = getGame(gameId)
                val player = getSantaUserPlayerFromUpdate(update)
                var status = false
                if (game.players.none { it.id == player.id }) {
                    game.players += player
                    status = true
                }
                mongoTemplate.save(game)
                tgOperations.replyToCurrentMessage(
                    if (status) "Вы зарегистрировались в игре \"$gameId\"" else "Вы уже зарегистрированы в игре"
                )
            }

            "/ignore" -> {
                val gameId = args[0]
                val ignored = args[1]
                val game = getGame(gameId)
                val playerId = getSantaUserPlayerFromUpdate(update).id
                val player = game.players.find { it.id == playerId }
                    ?: throw IllegalArgumentException("register in game first")
                player.ignores += ignored.replace("@", "").lowercase()
                mongoTemplate.save(game)
                tgOperations.replyToCurrentMessage(
                    "Вы добавили @${ignored} как свою вторую половинку! " +
                            "Теперь вы не будете дарить ему подарок, а он не будет дарить подарок вам."
                )
            }

            "/startgame" -> {
                val gameId = args.first()
                val game = getGame(gameId)
                startGame(game)
            }
        }
        return true
    }

    private suspend fun getGame(gameId: String): SecretSantaGame {
        return mongoTemplate.findById<SecretSantaGame>(gameId) ?: throw IllegalArgumentException("game not found")
            .also { tgOperations.replyToCurrentMessage("Игра не найдена") }
    }

    private suspend fun startGame(game: SecretSantaGame) {
        val users = game.players
        val targets = game.players.map { it.username }.toMutableList()

        val playerCount = users.size
        var iteration = 0
        val iterationLimit = playerCount * playerCount * 2

        while (!isConditionOk(users, targets) && iteration < iterationLimit) {
            val a = rand.nextInt(playerCount)
            val b = rand.nextInt(playerCount)
            val tmp = targets[a]
            targets[a] = targets[b]
            targets[b] = tmp
            iteration++
        }

        if (iteration >= iterationLimit) {
            tgOperations.replyToCurrentMessage("Не удалось найти подходящие пары. Попробуйте еще раз")
            return
        } else {
            users.zip(targets).forEach { (user, target) ->
                log.info("${user.username} дарит $target")
                tgOperations.sendMessage(
                    user.id,
                    "Ваша цель: @$target"
                )
            }
        }
    }

    fun isConditionOk(users: List<SecretSantaPlayer>, targets: List<String>, nonPairGifting: Boolean = true): Boolean {
        val userPairs = users.zip(targets)
        val individualUserConditionsMet = userPairs.all { (user, target) ->

            val iDontIgnoreTarget = user.ignores.none {
                target.lowercase().contains(it.lowercase())
            }

            val targetDoesntIgnoreMe = users.find { it.username == target }?.ignores?.none {
                user.username.lowercase().contains(it.lowercase())
            } != false

            val itsNotMe = user.username.lowercase() != target.lowercase()

            return@all iDontIgnoreTarget && targetDoesntIgnoreMe && itsNotMe
        }

        val hasCircles = userPairs.any { (user, target) ->
            userPairs.find { (isTarget, _) -> isTarget.username == target }?.second == user.username
        }
        val circularConditionMet = !nonPairGifting || !hasCircles

        return individualUserConditionsMet && circularConditionMet
    }

    private fun getSantaUserPlayerFromUpdate(update: Update): SecretSantaPlayer {
        return SecretSantaPlayer(
            id = update.message?.from?.id ?: throw IllegalArgumentException("not expected"),
            username = update.message.from.username ?: throw IllegalArgumentException("you must have a username")
        )
    }
}

class SecretSantaGame {
    var id: String = UUID.randomUUID().toString().substring(0 until 8)
    var players: List<SecretSantaPlayer> = listOf()
}

data class SecretSantaPlayer(
    var id: Long,
    var username: String
) {
    var ignores = setOf<String>()
}