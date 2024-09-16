package com.jamhour.commands;

import com.jamhour.core.job.Job;
import com.jamhour.util.pagination.DiscordPagination;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;


public class JobsSlashCommand implements SlashCommandCreateListener {
    public static final String COMMAND_NAME = "jobs";
    private final DiscordPagination<Job> discordPagination;

    public JobsSlashCommand(DiscordApi api, DiscordPagination<Job> discordPagination) {
        this.discordPagination = discordPagination;

        SlashCommand.with(COMMAND_NAME, "Get a list of available jobs.").createGlobal(api);
        api.addSlashCommandCreateListener(this);
        api.addMessageComponentCreateListener(discordPagination);
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

        slashCommandInteraction.getChannel().ifPresent(discordPagination::sendPaginatedMessage);
    }

}
