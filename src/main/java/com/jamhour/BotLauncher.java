package com.jamhour;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;

public class BotLauncher {
    public static void main(String[] args) {

        DiscordApi api = new DiscordApiBuilder()
                .setToken(System.getenv("BOT_TOKEN"))
                .addIntents(Intent.MESSAGE_CONTENT)
                .login()
                .join();

        new FilterJobsByCommand(api);
        new JobsSlashCommand(api);
    }
}