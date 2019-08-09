package com.proximyst.banvictus.bukkit

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import com.mrpowergamerbr.temmiewebhook.embed.FieldEmbed
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed
import litebans.api.Entry
import litebans.api.Events
import org.bukkit.ChatColor
import java.text.DateFormat
import java.util.*

class EmbedFieldEventListener(private val main: Banvictus) : Events.Listener() {
    val mainHook = TemmieWebhook(main.config.getString("webhooks.main"))
    val silentHook = main.config.getString("webhooks.silent")?.let {
        TemmieWebhook(it)
    }
    val avatar = main.config.getString(
        "avatar",
        "https://www.harborfreight.com/media/catalog/product/i/m/image_21582.jpg"
    )!!

    val footerIcon = main.config.getString("footer.icon")
    val footerText = main.config.getString("footer.text")

    @Suppress("RedundantLambdaArrow") // how about you let me do that without erring
    override fun entryAdded(entry: Entry) {
        val hook = if (entry.isSilent) {
            silentHook
        } else {
            mainHook
        } ?: return

        val colour = Integer.valueOf(main.config.getString("colours.${entry.type}"), 16)
        val uuid = try {
            UUID.fromString(entry.uuid)
        } catch (ex: IllegalArgumentException) {
            main.logger.warning { "${entry.uuid} is not a valid UUID" }
            return
        }
        val playerName = main.server.getOfflinePlayer(uuid).let {
            it.name ?: it.uniqueId.toString()
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
                if (footerIcon == null || footerText == null) {
                    return@let it
                }
                it.footer(
                    FooterEmbed.builder()
                        .icon_url(footerIcon)
                        .text(footerText)
                        .build()
                )
            }
            .build()

        val message = DiscordMessage.builder()
            .username("Minevictus Punishments")
            .avatarUrl(avatar)
            .embeds(listOf(embed))
            .build()

        hook.sendMessage(message)
    }
}