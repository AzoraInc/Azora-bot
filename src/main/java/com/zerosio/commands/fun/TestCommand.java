package com.zerosio.commands.fun;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import com.zerosio.commands.meta.Command;
import com.zerosio.commands.meta.CommandVisibility;
import com.zerosio.utility.Rank;

public class TestCommand extends Command {
    
    @Override
    public String getName() {
        return "test";
    }
    
    @Override
    public String getDescription() {
        return "some test stuff";
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
    public void execute(SlashCommandInteractionEvent event) {
        event.reply(event.getMember().getUser().getName()).setEphemeral(true).queue();
    }
}