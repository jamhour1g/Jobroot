package com.jamhour.util.pagination

import kotlin.math.ceil

const val DISCORD_MESSAGE_LIMIT = 5

class ItemPaginator<T>(
    private val items: List<T>,
    private val pageSize: Int = DISCORD_MESSAGE_LIMIT,
) {
    val totalPages: Int = ceil(items.size / pageSize.toDouble()).toInt()
    val itemPages: List<ItemPage<T>> = items.generatePages()

    private fun List<T>.generatePages(): List<ItemPage<T>> {
        return buildList {
            repeat(totalPages) {
                val jobs = items.subList(
                    it * pageSize,
                    minOf((it + 1) * pageSize, items.size)
                )
                add(ItemPage(it, pageSize, jobs))
            }
        }
    }

    operator fun get(pageNum: Int): ItemPage<T>? = itemPages.getOrNull(pageNum)
}

data class ItemPage<T>(
    val pageNum: Int,
    val totalPages: Int,
    val list: List<T>
) {
    val prevPageNum: Int = if (pageNum == 0) -1 else (pageNum - 1) % totalPages
    val nextPageNum: Int = (pageNum + 1) % totalPages

    fun shouldAddNextButton() = pageNum != totalPages - 1
    fun shouldAddPreviousButton() = prevPageNum != -1
}