package com.zerosio.commands.punishment;

import com.zerosio.Constants;
import com.zerosio.commands.meta.Command;
import com.zerosio.commands.meta.CommandVisibility;
import com.zerosio.utility.Rank;
import com.zerosio.utility.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends Command {

    @Override
    public String getName() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Temporarily mute a user (timeout)";
    }

    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PRIVATE;
    }

    @Override
    public Rank getRequiredRank() {
        return Rank.HELPER;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOption(OptionType.USER, "user", "The user to mute", true)
                .addOption(OptionType.STRING, "duration", "Duration like 10m, 2h, 3d", true)
                .addOption(OptionType.STRING, "reason", "Reason for muting", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        Member target = event.getOption("user").getAsMember();
        Member mod = event.getMember();
        String durationInput = event.getOption("duration").getAsString();
        String reason = event.getOption("reason").getAsString();

        if (target == null) {
            event.getHook().sendMessage("Could not find the user.").queue();
            return;
        }

        long durationMillis = parseDuration(durationInput);
        if (durationMillis <= 0 || durationMillis > TimeUnit.DAYS.toMillis(28)) {
            event.getHook().sendMessage("Invalid duration. Max timeout is 28 days.").queue();
            return;
        }

        Instant now = Instant.now();
        Instant until = now.plusMillis(durationMillis);
        String formattedTime = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a")
                .withZone(ZoneId.of("GMT"))
                .format(now);

        target.timeoutFor(Duration.ofMillis(durationMillis)).queue(
                success -> {
                    logMute(mod, target, reason, formattedTime, durationInput, event);
                    event.getHook().sendMessage(target.getUser().getAsMention() + " has been muted for " + durationInput + ".").setEphemeral(true).queue();
                },
                error -> event.getHook().sendMessage("Failed to mute the user.").queue()
        );
    }

    private long parseDuration(String input) {
        try {
            char unit = input.toLowerCase().charAt(input.length() - 1);
            long value = Long.parseLong(input.substring(0, input.length() - 1));

            switch (unit) {
                case 'm': return TimeUnit.MINUTES.toMillis(value);
                case 'h': return TimeUnit.HOURS.toMillis(value);
                case 'd': return TimeUnit.DAYS.toMillis(value);
                default: return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    private void logMute(Member mod, Member target, String reason, String time, String durationStr, SlashCommandInteractionEvent event) {
        // Logging embed
        EmbedBuilder logEmbed = new EmbedBuilder()
                .setTitle("Timeout Logs")
                .setDescription("**User**: " + target.getUser().getName() + "\n"
                        + "**Moderator**: " + mod.getUser().getName() + "\n"
                        + "**Duration**: " + durationStr + "\n"
                        + "**Reason**: " + reason + "\n"
                        + "**Time**: " + time + " (GMT)")
                .setFooter("Mod: " + mod.getUser().getName() + " | " + time + "\nIf you think it's false create a ticket.", mod.getUser().getAvatarUrl());

        Utils.sendEmbedToChannel(Constants.MUTE_LOGS, logEmbed);
        Utils.sendEmbedToChannel(Constants.AZORA_MUTE_LOGS, logEmbed);

        // Channel embed
        EmbedBuilder publicEmbed = new EmbedBuilder()
                .setTitle("User Muted")
                .setDescription("**User**: " + target.getUser().getName() + "\n"
                        + "**Duration**: " + durationStr + "\n"
                        + "**Reason**: " + reason + "\n"
                        + "**Moderator**: " + mod.getUser().getName())
                .setFooter("Mod: " + mod.getUser().getName() + " | " + time + "\nIf you think it's false create a ticket.", mod.getUser().getAvatarUrl());

        Utils.sendEmbedToChannel(event.getChannelId(), publicEmbed);

        // DM user
        EmbedBuilder dmEmbed = new EmbedBuilder()
                .setTitle("You've been muted.")
                .setDescription("Duration: **" + durationStr + "**\nReason: **" + reason + "**")
                .setFooter("Mod: " + mod.getUser().getName() + " | " + time + "\nIf you think it's false create a ticket.", mod.getUser().getAvatarUrl());

        try {
            target.getUser().openPrivateChannel().queue(channel -> {
                channel.sendMessageEmbeds(dmEmbed.build()).queue();
            });
        } catch (Exception ignored) {
            event.getHook().sendMessage("Could not DM the user.").setEphemeral(true).queue();
        }
    }
}
