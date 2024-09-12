package com.jamhour.commands;

import com.jamhour.util.JobProvidersKt;
import com.jamhour.util.Utilities;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class JobsSlashCommand implements SlashCommandCreateListener {

    public static final String COMMAND_NAME = "jobs";

    public JobsSlashCommand(DiscordApi api) {
        SlashCommand.with(COMMAND_NAME, "Get a list of available jobs.").createGlobal(api);
        api.addSlashCommandCreateListener(this);
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent slashCommandCreateEvent) {
        SlashCommandInteraction slashCommandInteraction = slashCommandCreateEvent.getSlashCommandInteraction();

        if (!slashCommandInteraction.getFullCommandName().equals(COMMAND_NAME)) {
            return;
        }

        slashCommandInteraction.createImmediateResponder()
                .setContent("Sending jobs...")
                .respond();

        slashCommandInteraction.getChannel()
                .ifPresent(channel ->
                        JobProvidersKt.getJobsAsCompletableFuture().join()
                                .forEach(job -> channel.sendMessage(Utilities.convertJobToEmbed(job)))
                );

    }
}
