package com.jamhour.util.pagination

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.component.LowLevelComponent
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.interaction.MessageComponentCreateEvent
import org.javacord.api.listener.interaction.MessageComponentCreateListener
import java.util.concurrent.CompletableFuture
import kotlin.collections.buildList

class DiscordPagination<T>(
    itemPaginator: () -> ItemPaginator<T>,
    private val pageContentConfig: PageContentConfig<T>,
    private val pageIndexExtractorFromComponentId: (String) -> Int?
) : MessageComponentCreateListener {

    val paginator by lazy { itemPaginator() }

    fun TextChannel.sendPaginatedMessage() = sendPaginatedMessage(0, this)

    override fun onComponentCreate(event: MessageComponentCreateEvent) {
        val interaction = event.messageComponentInteraction
        interaction.acknowledge()

        val pageIndex = pageIndexExtractorFromComponentId(interaction.customId) ?: return

        interaction.channel.ifPresent { sendPaginatedMessage(pageIndex, it) }

    }

    private fun sendPaginatedMessage(
        pageIndex: Int,
        channel: TextChannel
    ): CompletableFuture<Message?> =
        paginator[pageIndex]
            ?.createMessageBuilder(pageContentConfig)
            ?.send(channel) ?: CompletableFuture.completedFuture(null)
}

data class PageContentConfig<T>(
    val nextIdFactory: (ItemPage<T>) -> String = { "next-page@${it.nextPageNum}" },
    val prevIdFactory: (ItemPage<T>) -> String = { "prev-page@${it.prevPageNum}" },
    val embedFactory: (T) -> EmbedBuilder,
)

private fun <T> ItemPage<T>.createMessageBuilder(
    pageContentConfig: PageContentConfig<T>
) = MessageBuilder().apply {
    setContent("You are on page ${pageNum + 1} of $totalPages.")
    addEmbeds(list.map { pageContentConfig.embedFactory(it) })
    val components = buildList<LowLevelComponent> {

        if (shouldAddPreviousButton()) {
            add(
                Button.secondary(
                    pageContentConfig.prevIdFactory(this@createMessageBuilder),
                    "Back to Page ${prevPageNum + 1}"
                )
            )
        }

        if (shouldAddNextButton()) {
            add(
                Button.success(
                    pageContentConfig.nextIdFactory(this@createMessageBuilder),
                    "Go to Page ${nextPageNum + 1}"
                )
            )
        }

    }
    addComponents(ActionRow.of(components))
}