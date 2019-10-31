package com.proximyst.banvictus.bungee

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed
import com.proximyst.banvictus.EntryHandler
import com.proximyst.banvictus.fetching.AshconApi
import litebans.api.Entry
import org.bukkit.ChatColor
import java.text.DateFormat
import java.util.*

class TextEntryHandler : EntryHandler<Banvictus> {
    override fun handle(entry: Entry, plugin: Banvictus): Boolean {
        val hook = if (entry.isSilent) {
            plugin.silentHook
        } else {
            plugin.otherHooks[entry.type]
                ?: plugin.mainHook
        } ?: return false

        val colour = Integer.valueOf(plugin.config.getString("colours.${entry.type}"), 16)
        val user = AshconApi.fetchUser(entry.uuid ?: return false) ?: return false
        val date = Date(entry.dateStart)

        val punishmentValue = if (entry.duration == -1L || entry.type.equals("warn", true)) {
            ""
        } else {
            "Temporary "
        } + entry.type.capitalize()

        val text = """
            |Punishment: $punishmentValue
            |Time: ${DateFormat.getDateTimeInstance().format(date)}
            ${if (!entry.type.equals("warn", true)
            && !entry.type.equals("kick", true)
        ) {
            "|Duration: " +
                    if (entry.duration == -1L) {
                        "Permanent"
                    } else {
                        ChatColor.stripColor(entry.durationString)
                    } +
                    "\n"
        } else {
            ""
        }}|
        |Reason: ${entry.reason}
        |Punished by: ${entry.executorName}
        |
        |If you have any concerns, please message <@92332274552438784>.
        """.trimMargin()

        val embed = DiscordEmbed.builder()
            .title(user.username)
            .color(colour)
            .description(text)
            .let {
                if (plugin.footerIcon.isBlank() || plugin.footerText.isBlank()) {
                    return@let it
                }
                it.footer(
                    FooterEmbed.builder()
                        .icon_url(plugin.footerIcon)
                        .text(plugin.footerText)
                        .build()
                )
            }
            .build()

        val message = DiscordMessage.builder()
            .username("Minevictus Punishments")
            .avatarUrl(plugin.avatar)
            .embed(embed)
            .content("")
            .build()
        hook.sendMessage(message)

        return true
    }
}