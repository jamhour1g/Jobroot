package com.jamhour.util.pagination

import com.jamhour.core.job.Job
import com.jamhour.util.Utilities
import com.jamhour.util.getJobsAsCompletableFuture
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun jobsSlashCommandPaginator(): DiscordPagination<Job> {
    val jobsAsCompletableFuture = getJobsAsCompletableFuture()
    return DiscordPagination(
        { ItemPaginator(jobsAsCompletableFuture.join()) },
        PageContentConfig { Utilities.convertJobToEmbed(it) }
    ) {
        val matcher = PageContentConfig.defaultPattern.matcher(it)

        if (!matcher.matches()) {
            return@DiscordPagination null
        }

        return@DiscordPagination matcher.group(2).toIntOrNull()
    }
}