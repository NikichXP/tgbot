package com.nikichxp.tgbot.handlers.commands

import com.nikichxp.tgbot.dto.Update
import com.nikichxp.tgbot.entity.TgBot
import com.nikichxp.tgbot.service.menu.CommandHandler
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.UUID

// todo fuck clean code when you're out of time; whoever see this: remind me to finally refactor this shit
@Service
class SantaBotCommandHandler(
    private val mongoTemplate: MongoTemplate
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
            }
            "/register" -> {
                val gameId = args.first()
                val game = mongoTemplate.findById<SecretSantaGame>(gameId) ?: throw IllegalArgumentException("game not found")
                val player = getSantaUserPlayerFromUpdate(update)
                if (game.players.none { it.id == player.id }) {
                    game.players += player
                }
                mongoTemplate.save(game)
            }
            "/ignore" -> {
                val gameId = args[0]
                val ignoreId = args[1]
                // TODO have no time for this
            }
        }
        return true
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
    var ignores = listOf<String>()
}