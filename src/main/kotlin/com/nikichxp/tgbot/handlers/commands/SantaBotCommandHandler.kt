package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.service.menu.CommandHandler
import com.nikichxp.tgbot.service.tgapi.TgOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.UUID

// todo fuck clean code when you're out of time; whoever see this: remind me to finally refactor this shit
@Service
class SantaBotCommandHandler(
    private val mongoTemplate: MongoTemplate,
    private val tgOperations: TgOperations
) : CommandHandler {

    override fun supportedBots(tgBot: TgBot) = setOf(TgBot.SANTABOT)

    override fun isCommandSupported(command: String): Boolean = true

    override fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        when (command) {
            "/create" -> {
                val game = SecretSantaGame()
                val player = getSantaUserPlayerFromUpdate(update)
                game.players += player
                mongoTemplate.save(game)
                tgOperations.replyToCurrentMessage("game created", update)
            }

            "/register" -> {
                val gameId = args.first()
                val game = getGame(gameId, update)
                val player = getSantaUserPlayerFromUpdate(update)
                var status = false
                if (game.players.none { it.id == player.id }) {
                    game.players += player
                    status = true
                }
                mongoTemplate.save(game)
                tgOperations.replyToCurrentMessage(
                    if (status) "Вы зарегистрировались в игре" else "Вы уже зарегистрированы в игре",
                    update
                )
            }

            "/ignore" -> {
                val gameId = args[0]
                val ignored = args[1]
                val game = getGame(gameId, update)
                val playerId = getSantaUserPlayerFromUpdate(update).id
                val player = game.players.find { it.id == playerId } ?: throw IllegalArgumentException("register in game first")
                player.ignores += ignored
                mongoTemplate.save(game)
            }

            "/start" -> {
                val gameId = args.first()
                val game = getGame(gameId, update)
                startGame(game, update)
            }
        }
        return true
    }

    private fun getGame(gameId: String, update: Update): SecretSantaGame {
        return mongoTemplate.findById<SecretSantaGame>(gameId) ?: throw IllegalArgumentException("game not found")
            .also { tgOperations.replyToCurrentMessage("Игра не найдена", update) }
    }

    private fun startGame(game: SecretSantaGame, update: Update) {
        val users = game.players
        val targets = game.players.map { it.username }

        fun checkResult(): Boolean {

        }

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