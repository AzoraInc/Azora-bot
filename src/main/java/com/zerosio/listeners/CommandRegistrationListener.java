package com.zerosio.listeners;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.zerosio.Constants;
import com.zerosio.utility.Utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandRegistrationListener extends ListenerAdapter {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
	String formattedTime = LocalDateTime.now().format(formatter);

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		OffsetDateTime creationTime = event.getUser().getTimeCreated();
		String formattedCreationDate = formatAccountAge(creationTime);


		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle("Information");
		embed.setColor(Color.GREEN);
		embed.setDescription(event.getUser().getAsMention() + " has joined Azora.\n\n" + formattedCreationDate);
		embed.setFooter(formattedTime);
		embed.setThumbnail(event.getMember().getAvatarUrl());


		Utils.sendEmbedToChannel(Constants.JOIN_LEAVE_LOGS, embed);
		Utils.sendEmbedToChannel(Constants.AZORA_JOIN_LEAVE_LOGS, embed);
	}

	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {

	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {

	}

	private void createEmbed(String title, String footer, String azoraLog, String normalLog, CharSequence description) {
		EmbedBuilder embed = new EmbedBuilder();
		embed.setTitle(title);
		embed.setDescription(description);
		embed.setFooter(footer, null);


		Utils.sendEmbedToChannel(azoraLog, embed);
		Utils.sendEmbedToChannel(normalLog, embed);
	}

	public static String formatAccountAge(OffsetDateTime creationTime) {
		Duration duration = Duration.between(creationTime, OffsetDateTime.now(ZoneOffset.UTC));

		long seconds = duration.getSeconds();

		long years = seconds / (60 * 60 * 24 * 365);
		seconds %= (60 * 60 * 24 * 365);

		long months = seconds / (60 * 60 * 24 * 30);
		seconds %= (60 * 60 * 24 * 30);

		long days = seconds / (60 * 60 * 24);
		seconds %= (60 * 60 * 24);

		long hours = seconds / (60 * 60);
		seconds %= (60 * 60);

		long minutes = seconds / 60;
		seconds %= 60;

		return String.format("%d year(s), %d month(s), %d day(s), %d hour(s), %d minute(s), %d second(s)",
							 years, months, days, hours, minutes, seconds);
	}

}