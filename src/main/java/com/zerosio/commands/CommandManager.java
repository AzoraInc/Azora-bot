package com.zerosio.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.zerosio.Constants;
import com.zerosio.commands.fun.*;
import com.zerosio.commands.meta.*;
import com.zerosio.commands.punishment.*;
import com.zerosio.utility.*;

public class CommandManager extends ListenerAdapter {

	private static final Map<String, Command> commandMap = new HashMap<>();

	public CommandManager() {
//		register(new PublicCommand());
//		register(new TestCommand());
		register(new BanCommand());
		register(new MuteCommand());
	}

	private static void register(Command cmd) {
		commandMap.put(cmd.getName(), cmd);
	}

	public static void registerGuildCommands(JDA jda, String guildId) {
		Guild guild = jda.getGuildById(guildId);
		if (guild == null) return;

		//List<CommandData> commands = commandMap.values().stream().map(cmd -> {
//			CommandData data = cmd.getCommandData();
//			data.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
//			return data;
//		}).collect(Collectors.toList());
		List<CommandData> commands = commandMap.values().stream().map(cmd -> {
			Permission perm = cmd.getRequiredRank().getRequiredPermission();
			CommandData data = cmd.getCommandData();

			if (perm != null) {
				data.setDefaultPermissions(DefaultMemberPermissions.enabledFor(perm));
			} else {
				data.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
			}

			return data;
		}).collect(Collectors.toList());



		guild.updateCommands().addCommands(commands).queue();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		Command cmd = commandMap.get(event.getName());
		Rank requiredRank = cmd.getRequiredRank();
		Member member = event.getMember();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
		String formattedTime = LocalDateTime.now().format(formatter);


		if (cmd == null) return;

		if (cmd.getVisibility() == CommandVisibility.PRIVATE) {
			if (member == null) {
				event.reply("You must be in a guild to use this command.").setEphemeral(true).queue();
				return;
			}

			List<String> roleIds = member.getRoles().stream().map(role -> role.getId()).collect(Collectors.toList());
			Rank memberRank = Rank.getHighestRankFromRoles(roleIds);

			if (!memberRank.isAboveOrEqual(requiredRank)) {
				event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
				return;
			}
		}

		cmd.execute(event);

		if (requiredRank.isAboveOrEqual(Rank.ADMIN)) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Admin Commands Logger (Discord)");
			embed.setDescription("**" + member.getEffectiveName() + "** used /" + cmd.getName());
			embed.setColor(Color.RED);
			embed.setFooter("Admin: " + member.getUser().getEffectiveName() + " | " + formattedTime, member.getAvatarUrl());

			// embed.addField("Field Title", "Field Value", false);

			Utils.sendEmbedToChannel(Constants.ADMIN_COMMAND_LOGS, embed);
			Utils.sendEmbedToChannel(Constants.AZORA_ADMIN_COMMAND_LOGS, embed);
		}
	}

	public static Map<String, Command> getCommands() {
		return new HashMap<>(commandMap);
	}
}
