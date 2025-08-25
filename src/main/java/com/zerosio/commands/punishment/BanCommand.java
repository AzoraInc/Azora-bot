package com.zerosio.commands.punishment;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import com.zerosio.Constants;
import com.zerosio.commands.meta.Command;
import com.zerosio.commands.meta.CommandVisibility;
import com.zerosio.utility.Rank;
import com.zerosio.utility.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BanCommand extends Command {

	private static final long COOLDOWN = 15 * 60 * 1000; // 15 minutes
	private final Map<Long, Long> lastUsed = new HashMap<>();
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
	String formattedTime = LocalDateTime.now().format(formatter);

	@Override
	public String getName() {
		return "ban";
	}

	@Override
	public String getDescription() {
		return "Ban a user from the server.";
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.PRIVATE;
	}

	@Override
	public Rank getRequiredRank() {
		return Rank.ADMIN;
	}

	@Override
	public CommandData getCommandData() {
		return Commands.slash(getName(), getDescription())
			   .addOption(OptionType.USER, "user", "Target User", true)
			   .addOption(OptionType.STRING, "reason", "Reason for the ban", false);
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		Long executorId = event.getUser().getIdLong();
		Member member = event.getMember();
		long now = System.currentTimeMillis();

		if (lastUsed.containsKey(executorId)) {
			long last = lastUsed.get(executorId);
			if ((now - last) < COOLDOWN) {
				event.reply("You are on cooldown!")
				.setEphemeral(true).queue();
				return;
			}
		}

		Member target = event.getOption("user") != null ? event.getOption("user").getAsMember() : null;
		String reason = event.getOption("reason") != null
						? event.getOption("reason").getAsString()
						: "no reason given";

		if (target == null) {
			event.reply("User not found.").setEphemeral(true).queue();
			return;
		}

		if (!event.getGuild().getSelfMember().hasPermission(Permission.BAN_MEMBERS)) {
			event.reply("Ahem.. gib perms brehhh").setEphemeral(true).queue();
			return;
		}

		event.getGuild().ban(target, 0, TimeUnit.DAYS)
		.reason(reason)
		.queue(
		success -> {
			lastUsed.put(executorId, now);
			logMessage(member, target, reason, event);
			event.reply("Successfully banned **" + target.getUser().getName() + "**!").setEphemeral(true).queue();
		},
		error -> event.reply("Failed to ban user: " + error.getMessage()).setEphemeral(true).queue()
		);

	}

	public void logMessage(Member member, Member target, String reason, SlashCommandInteractionEvent e) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Ban logs");
		embed.setDescription("" +
							 "**User punished**: " + target.getUser().getName() + "\n" +
							 "**Responsible Moderator**: " + member.getUser().getName() + "\n" +
							 "**Reason**: " + reason + "\n\n" +
							 "**Time of execution**: " + formattedTime + " (GMT)");
		embed.setFooter("Mod: " + member.getUser().getName() + " | " + formattedTime, member.getUser().getAvatarUrl());


		Utils.sendEmbedToChannel(Constants.BAN_LOGS, embed);
		Utils.sendEmbedToChannel(Constants.AZORA_BAN_LOGS, embed);

		EmbedBuilder embed0 = new EmbedBuilder();

		embed0.setTitle("Successfully banned user!");
		embed0.setDescription("Violator: **" + target.getUser().getName() + "**\n" +
							  "Reason: **" + reason + "**");
		embed0.setFooter("Mod: " + member.getUser().getName() + " | " + formattedTime, member.getUser().getAvatarUrl());

		Utils.sendEmbedToChannel(e.getChannelId(), embed0);

		EmbedBuilder embed1 = new EmbedBuilder();

		embed1.setTitle("You've been banned.");
		embed1.setDescription("Punishment: **Permanent Ban**\n" +
							  "Reason: **" + reason + "**");
		embed1.setFooter("Mod: " + member.getUser().getName() + " | " + formattedTime + "\nIf you think it's false create a ticket.", member.getUser().getAvatarUrl());

		try {
			target.getUser().openPrivateChannel().queue(privateChannel -> {
				privateChannel.sendMessageEmbeds(embed1.build()).queue();
			});
		} catch (Exception eventrr) {
			e.reply("Target user's dms are closed, failed to send dm.").setEphemeral(true).queue();
		}

	}
}
