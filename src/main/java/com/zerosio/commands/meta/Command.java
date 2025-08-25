package com.zerosio.commands.meta;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import com.zerosio.utility.Rank;

public abstract class Command {

	public abstract String getName();
	public abstract String getDescription();
	public abstract CommandVisibility getVisibility();
	public abstract Rank getRequiredRank();

	public CommandData getCommandData() {
		return Commands.slash(getName(), getDescription());
	}

	public String getRequiredRankId() {
		Rank rank = getRequiredRank();
		return rank != null ? rank.getUUID() : null;
	}

	public abstract void execute(SlashCommandInteractionEvent event);

	public boolean isVisibleTo(SlashCommandInteractionEvent event) {
		if (getVisibility() == CommandVisibility.PUBLIC) {
			return true;
		}

		if (getRequiredRankId() == null) {
			return true;
		}

		return com.zerosio.utility.Utils.atLeast(event.getMember(), getRequiredRankId());
	}
}