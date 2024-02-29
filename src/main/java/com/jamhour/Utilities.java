package com.jamhour;

import com.jamhour.core.Job;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.net.URI;
import java.time.LocalDate;

public final class Utilities {

    private Utilities() {
    }

    public static EmbedBuilder convertJobToEmbed(Job job) {
        URI jobURI = job.getJobURI();
        String publishDate = job.getJobPublishDate().map(LocalDate::toString).orElse("");
        String deadline = job.getJobDeadline().map(LocalDate::toString).orElse("");

        String jobPosterWebsite = job.getJobPoster().getPosterWebsite().map(URI::toString).orElse("");

        return new EmbedBuilder()
                .setTitle(job.getJobTitle())
                .setAuthor(job.getJobPoster().getPosterName(), jobPosterWebsite, (String) null)
                .addField("Publish date - Deadline", publishDate + " - " + deadline)
                .addInlineField("Salary", job.getJobSalary())
                .addInlineField("Job Location", job.getJobLocation())
                .setUrl(jobURI == null ? jobPosterWebsite : jobURI.toString())
                .setColor(job.getJobsProvider().getProviderName().equals("Asal Technologies") ? Color.orange : Color.blue)
                .setFooter(job.getJobsProvider().getProviderName());
    }
}
