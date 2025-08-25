package com.zerosio.utility;

import com.zerosio.Bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Utils {
	public static boolean atLeast(Member member, String requiredRoleId) {
		Role required = member.getGuild().getRoleById(requiredRoleId);
		if (required == null) return false;

		for (Role role : member.getRoles()) {
			if (role.getPosition() >= required.getPosition()) {
				return true;
			}
		}
		return false;
	}

	public static void sendEmbedToChannel(String channelId, EmbedBuilder embed) {
		TextChannel channel = Bot.jda.getTextChannelById(channelId);
		if (channel != null) {
			channel.sendMessageEmbeds(embed.build()).queue();
		} else {
			System.out.println("Channel not found!");
		}
	}

}
