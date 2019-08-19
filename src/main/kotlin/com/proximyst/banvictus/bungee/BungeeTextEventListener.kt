package com.proximyst.banvictus.bungee

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed
import litebans.api.Entry
import litebans.api.Events
import net.md_5.bungee.api.ChatColor
import java.text.DateFormat
import java.util.*

class BungeeTextEventListener(private val main: Banvictus): EntryHandler {
    override fun handle(entry: Entry) {
        val hook = if (entry.isSilent) {
            main.silentHook
        } else {
            main.otherHooks[entry.type] ?: main.mainHook
        } ?: return

        val colour = Integer.valueOf(main.config.getString("colours.${entry.type}"), 16)
        val playerName = run {
            val body = HttpRequest.get("https://sessionserver.mojang.com/session/minecraft/profile/${entry.uuid?.replace("-", "")}")
            if (!body.ok()) {
                main.logger.warning { "Couldn't get profile for ${entry.uuid}; got error ${body.code()}" }
                return
            }
            val gs = main.gson.fromJson(body.body(), Map::class.java) as Map<String, Any>
            gs["name"] as String
        }
        val date = Date(entry.dateStart)

        val punishment = "${if (entry.duration == -1L) {
            ""
        } else {
            "Temporary "
        }}${entry.type}".capitalize()

        val text = """
            |Punishment: $punishment
            |Time: ${DateFormat.getDateTimeInstance().format(date)}
            |Duration: ${if (entry.duration == -1L) {
            "Permanent"
        } else {
            ChatColor.stripColor(entry.durationString)
        }}
            |
            |Reason: ${entry.reason}
            |Punished by: ${entry.executorName}
            |
            |If you have any concerns, please message <@92332274552438784>.
        """.trimMargin()

        val embed = DiscordEmbed.builder()
            .title(playerName)
            .color(colour)
            .description(text)
            .let {
                if (main.footerIcon.isBlank() || main.footerText.isBlank()) {
                    return@let it
                }
                it.footer(
                    FooterEmbed.builder()
                        .icon_url(main.footerIcon)
                        .text(main.footerText)
                        .build()
                )
            }
            .build()

        val message = DiscordMessage.builder()
            .username("Minevictus Punishments")
            .avatarUrl(main.avatar)
            .embed(embed)
            .content("")
            .build()
        hook.sendMessage(message)
    }
}