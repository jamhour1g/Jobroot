package com.jamhour.commands;

import com.jamhour.util.JobsProviders;
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

        slashCommandInteraction.createImmediateResponder().respond();

        slashCommandInteraction.getChannel()
                .ifPresent(channel ->
                        JobsProviders.getJobs()
                                .forEach(job -> channel.sendMessage(Utilities.convertJobToEmbed(job)))
                );

    }
}
