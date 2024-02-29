package com.jamhour;

import com.jamhour.core.Job;
import com.jamhour.util.JobsProviders;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class FilterJobsByCommand implements SlashCommandCreateListener {

    public static final String COMMAND_NAME = "filter-jobs";

    private static final String JOB_POSTER_SUB_COMMAND = "job-poster";
    private static final String JOB_SUB_COMMAND = "job";
    private static final String JOB_TITLE_STRING_OPTION = "job-title";
    private static final String JOB_LOCATION_STRING_OPTION = "job-location";
    private static final String JOB_SALARY_STRING_OPTION = "job-salary";
    private static final String POSTER_NAME_STRING_OPTION = "poster-name";
    private static final String POSTER_LOCATION_STRING_OPTION = "poster-location";
    private static final String EXACTLY_MATCHING_SUB_COMMAND_GROUP = "exactly-matching";
    private static final String CONTAINING_SUB_COMMAND_GROUP = "containing";

    public FilterJobsByCommand(DiscordApi api) {
        SlashCommand.with(COMMAND_NAME, "Find your ideal job with tailored filters!",
                        List.of(
                                containingSubGroup(),
                                exactlyMatchingSubGroup()
                        )
                )
                .createGlobal(api);
        api.addSlashCommandCreateListener(this);
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent slashCommandCreateEvent) {
        SlashCommandInteraction slashCommandInteraction = slashCommandCreateEvent.getSlashCommandInteraction();

        if (!slashCommandInteraction.getCommandName().equals(COMMAND_NAME)) {
            return;
        }

        slashCommandInteraction.createImmediateResponder().respond();

        Optional<TextChannel> channelOptional = slashCommandInteraction.getChannel();
        if (channelOptional.isEmpty()) {
            return;
        }

        TextChannel channel = channelOptional.get();

        List<Predicate<Job>> filters = new ArrayList<>();

        slashCommandInteraction.getOptionByName(EXACTLY_MATCHING_SUB_COMMAND_GROUP)
                .ifPresent(commandInteractionOption -> handleExactlyMatchingSubCommandGroupFilters(commandInteractionOption, slashCommandInteraction, filters));

        slashCommandInteraction.getOptionByName(CONTAINING_SUB_COMMAND_GROUP)
                .ifPresent(commandInteractionOption -> handleContainingSubCommandGroupFilters(commandInteractionOption, slashCommandInteraction, filters));

        List<Job> filteredJobs = JobsProviders.getJobs();
        for (Predicate<Job> filter : filters) {
            filteredJobs = filteredJobs
                    .stream()
                    .filter(filter)
                    .toList();
        }

        if (filteredJobs.isEmpty()) {
            channel.sendMessage("No jobs matching your search criteria found!");
            return;
        }

        filteredJobs.forEach(job -> channel.sendMessage(Utilities.convertJobToEmbed(job)));
    }

    private static void handleExactlyMatchingSubCommandGroupFilters(SlashCommandInteractionOption slashCommandInteractionOption,
                                                                    SlashCommandInteraction slashCommandInteraction,
                                                                    List<Predicate<Job>> filters) {

        slashCommandInteractionOption.getOptionByName(JOB_SUB_COMMAND).ifPresent(jobSection -> handleJobExactlyMatchingFilters(slashCommandInteraction, filters));
        slashCommandInteractionOption.getOptionByName(JOB_POSTER_SUB_COMMAND).ifPresent(jobPoster -> handleJobPosterExactlyMatchingFilters(slashCommandInteraction, filters));
    }

    private static void handleJobPosterExactlyMatchingFilters(SlashCommandInteraction slashCommandInteraction, List<Predicate<Job>> filters) {
        slashCommandInteraction.getArgumentByName(POSTER_NAME_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobPosterName -> filters.add(job -> job.getJobPoster().getPosterName().equals(jobPosterName)));

        slashCommandInteraction.getArgumentByName(POSTER_LOCATION_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobPosterLocation -> filters.add(job -> job.getJobPoster().getPosterLocation().equals(jobPosterLocation)));
    }

    private static void handleJobExactlyMatchingFilters(SlashCommandInteraction slashCommandInteraction, List<Predicate<Job>> filters) {
        slashCommandInteraction.getArgumentByName(JOB_TITLE_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobTitle -> filters.add(job -> job.getJobTitle().equals(jobTitle)));

        slashCommandInteraction.getArgumentByName(JOB_LOCATION_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobLocation -> filters.add(job -> job.getJobLocation().equals(jobLocation)));

        slashCommandInteraction.getArgumentByName(JOB_SALARY_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobLocation -> filters.add(job -> job.getJobSalary().equals(jobLocation)));
    }

    private static void handleContainingSubCommandGroupFilters(SlashCommandInteractionOption slashCommandInteractionOption,
                                                               SlashCommandInteraction slashCommandInteraction,
                                                               List<Predicate<Job>> filters) {

        slashCommandInteractionOption.getOptionByName(JOB_SUB_COMMAND)
                .ifPresent(jobSection -> handleJobContainSubCommandFilters(slashCommandInteraction, filters));

        slashCommandInteractionOption.getOptionByName(JOB_POSTER_SUB_COMMAND)
                .ifPresent(jobPoster -> handleJobPosterSubCommandFilters(slashCommandInteraction, filters));

    }

    private static void handleJobPosterSubCommandFilters(SlashCommandInteraction slashCommandInteraction, List<Predicate<Job>> filters) {
        slashCommandInteraction.getArgumentByName(POSTER_NAME_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobPosterName -> filters.add(job -> job.getJobPoster().getPosterName().toLowerCase().contains(jobPosterName.toLowerCase())));

        slashCommandInteraction.getArgumentByName(POSTER_LOCATION_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobPosterLocation -> filters.add(job -> job.getJobPoster().getPosterLocation().toLowerCase().contains(jobPosterLocation.toLowerCase())));
    }

    private static void handleJobContainSubCommandFilters(SlashCommandInteraction slashCommandInteraction, List<Predicate<Job>> filters) {
        slashCommandInteraction.getArgumentByName(JOB_TITLE_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobTitle -> filters.add(job -> job.getJobTitle().toLowerCase().contains(jobTitle.toLowerCase())));

        slashCommandInteraction.getArgumentByName(JOB_LOCATION_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobLocation -> filters.add(job -> job.getJobLocation().toLowerCase().contains(jobLocation.toLowerCase())));

        slashCommandInteraction.getArgumentByName(JOB_SALARY_STRING_OPTION)
                .flatMap(SlashCommandInteractionOption::getStringValue)
                .ifPresent(jobLocation -> filters.add(job -> job.getJobSalary().toLowerCase().contains(jobLocation.toLowerCase())));
    }

    private static SlashCommandOption exactlyMatchingSubGroup() {
        return SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, EXACTLY_MATCHING_SUB_COMMAND_GROUP, "Find jobs that exactly match your criteria. Get the perfect fit!",
                List.of(
                        jobPosterCommands(),
                        jobCommands()
                )
        );
    }

    private static SlashCommandOption containingSubGroup() {
        return SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, CONTAINING_SUB_COMMAND_GROUP, "Narrow down your results by searching for specific terms (keywords) within job postings",
                List.of(
                        jobPosterCommands(),
                        jobCommands()
                )
        );
    }

    private static SlashCommandOption jobCommands() {
        return SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, JOB_SUB_COMMAND, "Dive into the details of specific job listings.",
                jobOptions()
        );
    }

    private static List<SlashCommandOption> jobOptions() {
        return List.of(
                SlashCommandOption.createStringOption(JOB_TITLE_STRING_OPTION, "Zero in on jobs with titles that align with your career path.", false),
                SlashCommandOption.createStringOption(JOB_LOCATION_STRING_OPTION, "Find opportunities in your preferred areas or those open to remote work.", false),
                SlashCommandOption.createStringOption(JOB_SALARY_STRING_OPTION, "Discover roles that meet your financial expectations.", false)
        );
    }

    private static SlashCommandOption jobPosterCommands() {
        return SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND, JOB_POSTER_SUB_COMMAND, "Focus on opportunities from particular companies or organizations.",
                jobPosterOptions()
        );
    }

    private static List<SlashCommandOption> jobPosterOptions() {
        return List.of(
                SlashCommandOption.createStringOption(POSTER_NAME_STRING_OPTION, "Specify the name of the job poster you're interested in.", false),
                SlashCommandOption.createStringOption(POSTER_LOCATION_STRING_OPTION, "Search for jobs based on the poster's location.", false)
        );
    }
}
