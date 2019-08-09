package com.proximyst.banvictus.bungee

import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.plugin.Command

class ReloadCommand(private val main: Banvictus) : Command("banvictus-reload") {
    override fun execute(sender: CommandSender, args: Array<out String>?) {
        if (!sender.hasPermission("banvictus.reload")) {
            sender.sendMessage(
                *ComponentBuilder("You are not authorized to do this.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }

        main.apply {
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
            } else {
                handler = BungeeFieldEventListener(this)
            }
        }
        sender.sendMessage("reloaded")
    }
}