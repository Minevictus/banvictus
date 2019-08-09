package com.proximyst.banvictus.bungee

import com.google.gson.Gson
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import litebans.api.Entry
import litebans.api.Events
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.File
import java.nio.file.Files

class Banvictus : Plugin() {
    lateinit var config: Configuration
        private set

    val gson = Gson()

    var mainHook: TemmieWebhook? = null
    var silentHook: TemmieWebhook? = null
    var avatar: String = ""

    var footerIcon: String = ""
    var footerText: String = ""

    var handler: EntryHandler = BungeeFieldEventListener(this)

    override fun onEnable() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }

        val configFile = File(dataFolder, "config.yml")

        if (!configFile.exists()) {
            getResourceAsStream("config.yml").use {
                Files.copy(it, file.toPath())
            }
        }

        config = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
            .load(configFile)

        mainHook = TemmieWebhook(config.getString("webhooks.main"))
        silentHook = config.getString("webhooks.silent")?.let {
            TemmieWebhook(it)
        }
        avatar = config.getString(
            "avatar",
            "https://www.harborfreight.com/media/catalog/product/i/m/image_21582.jpg"
        )!!
        footerIcon = config.getString("footer.icon") ?: ""
        footerText = config.getString("footer.text") ?: ""

        if (!config.getBoolean("use-embed-fields")) {
            handler = BungeeTextEventListener(this)
        }

        proxy.pluginManager.registerCommand(this, ReloadCommand(this))

        Events.get().register(object : Events.Listener() {
            override fun entryAdded(entry: Entry) {
                handler.handle(entry)
            }
        })
    }
}