package com.zerosio.commands.fun;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import com.zerosio.commands.meta.Command;
import com.zerosio.commands.meta.CommandVisibility;
import com.zerosio.utility.Rank;

public class PublicCommand extends Command {
    
    @Override
    public String getName() {
        return "public";
    }
    
    @Override
    public String getDescription() {
        return "A public command everyone can use";
    }
    
    @Override
    public CommandVisibility getVisibility() {
        return CommandVisibility.PUBLIC;
    }
    
    @Override
    public Rank getRequiredRank() {
        return null;
    }
    
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("Public command executed!").queue();
    }
}