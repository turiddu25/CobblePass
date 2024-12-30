package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StartCommand extends Subcommand {
    public StartCommand() {
        super("§9Usage:\n§3- /battlepass start");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("start")
            .requires(source -> source.hasPermission(4)) // Requires operator permission level
            .executes(this::run)
            .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // Check if a season is already active
        if (CobblePass.config.isSeasonActive()) {
            source.sendSystemMessage(Component.literal("§cA battle pass season is already active! Current season " + 
                CobblePass.config.getCurrentSeason() + " ends in " + 
                formatTimeRemaining(CobblePass.config.getSeasonEndTime() - System.currentTimeMillis())));
            return Command.SINGLE_SUCCESS;
        }

        // Start new season
        CobblePass.config.startNewSeason();
        
        // Broadcast to all players
        source.getServer().getPlayerList().getPlayers().forEach(player -> 
            player.sendSystemMessage(Component.literal("§6Battle Pass Season " + 
                CobblePass.config.getCurrentSeason() + " has begun!")));

        return Command.SINGLE_SUCCESS;
    }

    private String formatTimeRemaining(long milliseconds) {
        long seconds = milliseconds / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        
        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
    }
}
