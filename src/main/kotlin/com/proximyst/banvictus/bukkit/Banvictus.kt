package com.proximyst.banvictus.bukkit

import litebans.api.Events
import org.bukkit.plugin.java.JavaPlugin

class Banvictus : JavaPlugin() {
    override fun onEnable() {
        saveDefaultConfig()

        Events.get().register(
            if (config.getBoolean("use-embed-fields")) {
                EmbedFieldEventListener(this)
            } else {
                EmbedTextEventListener(this)
            }
        )
    }
}