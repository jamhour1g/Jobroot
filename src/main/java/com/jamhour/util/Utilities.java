package com.jamhour.util;

import com.jamhour.core.job.Job;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.net.URI;
import java.util.Random;

public final class Utilities {

    private static final Random random = new Random();

    private Utilities() {
    }

    public static EmbedBuilder convertJobToEmbed(Job job) {
        URI jobURI = job.getJobURI();
        String publishDate = job.getJobPublishDate() == null ? "N/A" : job.getJobPublishDate().toString();
        String deadline = job.getJobDeadline() == null ? "N/A" : job.getJobDeadline().toString();
        String jobPosterWebsite = job.getJobPoster().getPosterWebsite() == null ? "N/A" : job.getJobPoster().getPosterWebsite().toString();

        float[] hsbValues = Color.RGBtoHSB(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                null
        );

        String salary = job.getJobSalary().isBlank() ? "N/A" : job.getJobSalary();

        return new EmbedBuilder()
                .setTitle(job.getJobTitle())
                .setAuthor(job.getJobPoster().getPosterName(), jobPosterWebsite, (String) null)
                .addField("Publish date - Deadline", publishDate + " - " + deadline)
                .addInlineField("Salary", salary)
                .addInlineField("Job Location", job.getJobLocation())
                .setUrl(jobURI.toString())
                .setFooter(job.getJobProvider().getProviderName())
                .setColor(Color.getHSBColor(hsbValues[0], hsbValues[1], hsbValues[2]))
                .setTimestampToNow();
    }
}
