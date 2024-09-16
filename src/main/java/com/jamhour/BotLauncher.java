package com.jamhour;

import com.jamhour.commands.FilterJobsByCommand;
import com.jamhour.commands.JobsSlashCommand;
import com.jamhour.util.pagination.DiscordPaginatorsFactoryKt;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.intent.Intent;

public class BotLauncher {
    public static void main(String[] args) {

        DiscordApi api = new DiscordApiBuilder()
                .setToken(System.getenv("BOT_TOKEN"))
                .addIntents(Intent.MESSAGE_CONTENT)
                .login()
                .join();

        api.updateActivity(ActivityType.PLAYING, "/jobs");

        new FilterJobsByCommand(api);
        new JobsSlashCommand(api, DiscordPaginatorsFactoryKt.jobsSlashCommandPaginator());
    }
}