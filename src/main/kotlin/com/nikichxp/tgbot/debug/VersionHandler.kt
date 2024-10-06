package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.jar.Manifest

@Component
class VersionHandler(
    private val tgOperations: TgOperations,
) : CommandHandler {

    lateinit var version: String

    @PostConstruct
    fun loadManifestData() {
        try {
            val manifest = Manifest(
                javaClass.classLoader.getResourceAsStream("META-INF/MANIFEST.MF")
            )
            val attributes = manifest.mainAttributes
            this.version = attributes.getValue("Implementation-Version") ?: "unknown"
        } catch (e: Exception) {
            e.printStackTrace()
            this.version = "unknown"
        }
    }

    override fun supportedBots(tgBot: TgBot) = TgBot.entries.toSet()

    override fun isCommandSupported(command: String) = command in listOf("/version", "/v", "/buildinfo")

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        tgOperations.replyToCurrentMessage("version: $version")
        return true
    }
}

