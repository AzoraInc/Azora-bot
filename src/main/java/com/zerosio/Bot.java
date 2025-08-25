package com.zerosio;

import com.zerosio.commands.CommandManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class Bot {
    public static JDA jda;
    
    public static void main(String[] args) throws InterruptedException {
        jda = JDABuilder.createDefault(Constants.TOKEN)
                .addEventListeners(new CommandManager())
                .setStatus(OnlineStatus.IDLE)
                .setActivity(Activity.watching("Azora"))
                .build();

        jda.awaitReady();

        CommandManager.registerGuildCommands(jda, Constants.GUILD_ID);
        
        
    }
}
