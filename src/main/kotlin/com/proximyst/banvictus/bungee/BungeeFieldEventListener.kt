package com.proximyst.banvictus.bungee

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.embed.FieldEmbed
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed
import litebans.api.Entry
import net.md_5.bungee.api.ChatColor
import java.text.DateFormat
import java.util.*

class BungeeFieldEventListener(private val main: Banvictus) : EntryHandler {
    @Suppress("RedundantLambdaArrow") // how about you let me do that without erring
    override fun handle(entry: Entry) {
        val hook = if (entry.isSilent) {
            main.silentHook
        } else {
            main.mainHook
        } ?: return

        val colour = Integer.valueOf(main.config.getString("colours.${entry.type}"), 16)
        val playerName = run {
            val body = HttpRequest.get(
                "https://sessionserver.mojang.com/session/minecraft/profile/${entry.uuid?.replace(
                    "-",
                    ""
                )}"
            )
            if (!body.ok()) {
                main.logger.warning { "Couldn't get profile for ${entry.uuid}; got error ${body.code()}" }
                return
            }
            val gs = main.gson.fromJson(body.body(), Map::class.java) as Map<String, Any>
            gs["name"] as String
        }
        val date = Date(entry.dateStart)

        val punishmentField = FieldEmbed.builder()
            .inline(true)
            .name("Punishment")
            .value(
                "${if (entry.duration == -1L) {
                    ""
                } else {
                    "Temporary "
                }}${entry.type.capitalize()}"
            )
            .build()

        val timeField = FieldEmbed.builder()
            .inline(true)
            .name("Time")
            .value(DateFormat.getDateTimeInstance().format(date))
            .build()

        val durationField = FieldEmbed.builder()
            .inline(true)
            .name("Duration")
            .value(
                if (entry.duration == -1L) {
                    "Permanent"
                } else {
                    ChatColor.stripColor(entry.durationString)
                }
            )
            .build()

        val punisherField = FieldEmbed.builder()
            .inline(true)
            .name("Punished by")
            .value(entry.executorName)
            .build()

        val reasonField = FieldEmbed.builder()
            .inline(false)
            .name("Reason")
            .value(entry.reason)
            .build()

        val embed = DiscordEmbed.builder()
            .title(playerName)
            .color(colour)
            .field(punishmentField)
            .field(timeField)
            .field(durationField)
            .field(punisherField)
            .field(reasonField)
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
            .embeds(listOf(embed))
            .build()

        hook.sendMessage(message)
    }
}