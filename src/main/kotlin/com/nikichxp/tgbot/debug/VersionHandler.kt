package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.AppStorage
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.jar.Manifest

@Component
class VersionHandler(
    private val tgOperations: TgOperations,
    private val appStorage: AppStorage
) : CommandHandler {

    lateinit var version: String

    @Value("\${app.admin-id}")
    var adminId: Long = 0

    @PostConstruct
    suspend fun loadManifestData() {
        try {
            val manifest = Manifest(
                javaClass.classLoader.getResourceAsStream("META-INF/MANIFEST.MF")
            )
            val attributes = manifest.mainAttributes
            val appVersion: String? = attributes.getValue("Implementation-Version")
            this.version = appVersion ?: NULL_VERSION
            appVersion?.let {
                val previousVersion = appStorage.getData(VERSION_KEY)
                if (it != previousVersion?.value && adminId != 0L) {
                    appStorage.saveData(VERSION_KEY, it)
                    TgBot.entries.forEach { bot ->
                        tgOperations.sendMessage(
                            chatId = adminId,
                            text = "New version deployed: $it",
                            tgBot = bot
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            this.version = NULL_VERSION
        }
    }

    override fun supportedBots(tgBot: TgBot) = TgBot.entries.toSet()

    override fun isCommandSupported(command: String) = command in listOf("/version", "/v", "/buildinfo")

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        tgOperations.replyToCurrentMessage("version: $version")
        return true
    }

    companion object {
        const val NULL_VERSION = "unknown"
        const val VERSION_KEY = "app_version"
    }
}

