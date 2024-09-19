package com.jamhour.util.pagination

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.MessageUpdater
import org.javacord.api.entity.message.component.ActionRow
import org.javacord.api.entity.message.component.Button
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.interaction.ButtonClickEvent
import org.javacord.api.listener.interaction.ButtonClickListener
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern
import kotlin.collections.buildList

class DiscordPagination<T>(
    itemPaginator: () -> ItemPaginator<T>,
    private val pageContentConfig: PageContentConfig<T>,
    private val pageIndexExtractorFromButtonCustomId: (String) -> Int?
) : ButtonClickListener {

    val paginator by lazy { itemPaginator() }

    fun TextChannel.sendPaginatedMessage() =
        paginator[0]
            ?.createMessageBuilder()
            ?.send(this) ?: CompletableFuture.completedFuture(null)

    private fun MessageUpdater.updateMessage(
        customId: String
    ) {
        removeAllEmbeds()
        removeAllComponents()
        removeContent()

        val pageIndex = pageIndexExtractorFromButtonCustomId(customId) ?: return

        applyPageContent(pageIndex)
    }

    private fun MessageUpdater.applyPageContent(
        pageIndex: Int
    ) = paginator[pageIndex]?.run {
        setContent("You are on page ${pageNum + 1} of $totalPages.")
        addEmbeds(list.map { pageContentConfig.embedFactory(it) })
        val components = buildComponents(pageContentConfig)
        addComponents(ActionRow.of(components))
    }

    private fun MessageBuilder.applyPageContent(
        pageIndex: Int
    ) = paginator[pageIndex]?.run {
        setContent("You are on page ${pageNum + 1} of $totalPages.")
        addEmbeds(list.map { pageContentConfig.embedFactory(it) })
        val components = buildComponents(pageContentConfig)
        addComponents(ActionRow.of(components))
    }

    private fun <T> ItemPage<T>.createMessageBuilder() = MessageBuilder().applyPageContent(pageNum)

    override fun onButtonClick(event: ButtonClickEvent) {
        event.buttonInteraction.apply {
            message.createUpdater().run {
                removeAllEmbeds()
                removeAllComponents()
                removeContent()
                updateMessage(customId)
                applyChanges()
            }
            acknowledge()
        }
    }

}

data class PageContentConfig<T>(
    val nextIdFactory: (ItemPage<T>) -> String = { "next-page@${it.nextPageNum}" },
    val prevIdFactory: (ItemPage<T>) -> String = { "prev-page@${it.prevPageNum}" },
    val embedFactory: (T) -> EmbedBuilder,
) {
    companion object {
        val defaultPattern = Pattern.compile("(next|prev)-page@(-1|\\d+)")
    }
}

private fun <T> ItemPage<T>.buildComponents(pageContentConfig: PageContentConfig<T>) = buildList {
    if (shouldAddPreviousButton()) {
        add(
            Button.secondary(
                pageContentConfig.prevIdFactory(this@buildComponents),
                "Back to Page ${prevPageNum + 1}"
            )
        )
    }

    if (shouldAddNextButton()) {
        add(
            Button.success(
                pageContentConfig.nextIdFactory(this@buildComponents),
                "Go to Page ${nextPageNum + 1}"
            )
        )
    }
}
