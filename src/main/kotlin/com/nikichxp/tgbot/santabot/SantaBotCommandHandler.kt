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
                game.createdBy = update.message?.from?.id ?: throw IllegalArgumentException("Can't get user id")

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
                val game = getGame(gameId) ?: return noGameFound()
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

            "/players" -> {
                if (args.size != 1) {
                    tgOperations.replyToCurrentMessage("Используйте /players gameId")
                    return true
                }

                val gameId = args.first()
                val game = getGame(gameId) ?: return noGameFound()
                val players = game.players.joinToString("\n") { '@' + it.username }
                tgOperations.replyToCurrentMessage("Игроки в игре \"$gameId\":\n$players")
            }

            "/ignore" -> {
                if (args.size != 2) {
                    tgOperations.replyToCurrentMessage("Используйте /ignore gameId @username")
                    return true
                }

                val gameId = args[0]
                val ignored = args[1]
                val game = getGame(gameId) ?: return noGameFound()
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

            "/testgame" -> {
                val gameId = args.first()
                val game = getGame(gameId) ?: return noGameFound()

                if (game.createdBy != update.message?.from?.id) {
                    tgOperations.replyToCurrentMessage("Вы не создатель игры")
                    return true
                }
                
                val playerPairs = calculatePlayers(game)
                tgOperations.replyToCurrentMessage(
                    playerPairs.joinToString("\n") { (user, target) -> "@${user.username} -> @$target" }
                )
            }

            "/startgame" -> {
                val gameId = args.first()
                val game = getGame(gameId) ?: return noGameFound()

                when {
                    game.isStarted -> tgOperations.replyToCurrentMessage("Игра уже начата")
                    game.players.size < 3 -> tgOperations.replyToCurrentMessage("Недостаточно игроков")
                    game.createdBy != update.message?.from?.id -> tgOperations.replyToCurrentMessage("Вы не создатель игры")
                    else -> {
                        val playerPairs = calculatePlayers(game)
                        startGame(playerPairs)
                        game.isStarted = true
                        mongoTemplate.save(game)
                    }
                }
            }
        }
        return true
    }

    private suspend fun getGame(gameId: String): SecretSantaGame? {
        return mongoTemplate.findById<SecretSantaGame>(gameId)
    }

    private suspend fun noGameFound(): Boolean {
        tgOperations.replyToCurrentMessage("Игра не найдена")
        return true
    }

    private suspend fun calculatePlayers(game: SecretSantaGame): List<Pair<SecretSantaPlayer, String>> {
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
            return emptyList()
        } else {
            return users.zip(targets)
        }
    }

    private suspend fun startGame(playerPairs: List<Pair<SecretSantaPlayer, String>>) {
        playerPairs.forEach { (user, target) ->
            log.info("${user.username} дарит $target")
            tgOperations.sendMessage(
                user.id,
                "Ваша цель: @$target"
            )
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
    var isStarted = false
    var createdBy: Long = 0
}

data class SecretSantaPlayer(
    var id: Long,
    var username: String
) {
    var ignores = setOf<String>()
}