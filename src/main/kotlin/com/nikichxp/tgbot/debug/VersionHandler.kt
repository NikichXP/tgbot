package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.dto.Update
import com.nikichxp.tgbot.core.entity.TgBot
import com.nikichxp.tgbot.core.handlers.commands.CommandHandler
import com.nikichxp.tgbot.core.service.tgapi.TgOperations
import com.nikichxp.tgbot.core.util.AppStorage
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.jar.Manifest

@Component
class VersionHandler(
    private val tgOperations: TgOperations,
    private val appStorage: AppStorage,
    private val sendMessageToAdminService: SendMessageToAdminService,
    private val coroutineScope: CoroutineScope
) : CommandHandler {

    private val log = LoggerFactory.getLogger(this::class.java)

    private val appVersion: String by lazy { getManifestVersion() ?: NULL_VERSION }

    @PostConstruct
    fun loadManifestData() {
        if (appVersion == NULL_VERSION) {
            log.warn("Failed to load version from manifest")
            coroutineScope.launch {
                sendMessageToAdminService.sendMessage("Failed to load version from manifest")
            }
        } else {
            val previousVersion = appStorage.getData(VERSION_KEY)
            if (appVersion != previousVersion?.value) {
                appStorage.saveData(VERSION_KEY, appVersion)
                coroutineScope.launch {
                    sendMessageToAdminService.sendMessage("New version deployed: $appVersion")
                }
            }
        }
    }

    override fun supportedBots(tgBot: TgBot) = TgBot.entries.toSet()

    override fun isCommandSupported(command: String) = command in listOf("/version", "/v", "/buildinfo")

    override suspend fun processCommand(args: List<String>, command: String, update: Update): Boolean {
        tgOperations.replyToCurrentMessage("version: $appVersion")
        return true
    }

    private fun getManifestVersion(): String? {
        try {
            val manifest = Manifest(
                javaClass.classLoader.getResourceAsStream("META-INF/MANIFEST.MF")
            )
            val attributes = manifest.mainAttributes
            return attributes.getValue("Implementation-Version")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    companion object {
        const val NULL_VERSION = "unknown"
        const val VERSION_KEY = "app_version"
    }
}

