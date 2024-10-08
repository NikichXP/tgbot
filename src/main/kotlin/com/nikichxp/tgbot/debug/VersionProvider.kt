package com.nikichxp.tgbot.debug

import com.nikichxp.tgbot.core.util.AppStorage
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.jar.Manifest

@Component
class VersionProvider(
    private val appStorage: AppStorage,
    private val sendMessageToAdminService: SendMessageToAdminService,
    private val coroutineScope: CoroutineScope
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    val appVersion: String by lazy { loadManifestVersion() ?: NULL_VERSION }

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

    private fun loadManifestVersion(): String? {
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