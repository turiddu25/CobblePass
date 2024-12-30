package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PremiumCommand extends Subcommand {

    public PremiumCommand() {
        super("§9Usage:\n§3- /battlepass premium");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("premium")
                .executes(this::run)
                .then(Commands.literal("buy")
                    .executes(this::buyPremium))
                .then(Commands.literal("info")
                    .executes(this::showInfo))
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return showInfo(context);
    }

    private int buyPremium(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.literal("This command must be run by a player"));
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();

        // Check if season is active
        if (!CobblePass.config.isSeasonActive()) {
            player.sendSystemMessage(Component.literal("§cNo battle pass season is currently active!"));
            return 1;
        }

        // Check if player already has premium
        if (CobblePass.battlePass.getPlayerPass(player).hasPremium()) {
            player.sendSystemMessage(Component.literal("§cYou already have premium battle pass for this season!"));
            return 1;
        }

        // Grant premium
        CobblePass.battlePass.getPlayerPass(player).setPremium(true);
        player.sendSystemMessage(Component.literal("§aSuccessfully granted premium battle pass for Season " + 
            CobblePass.config.getCurrentSeason() + "!"));

        return 1;
    }

    private int showInfo(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.literal("This command must be run by a player"));
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        player.sendSystemMessage(Component.literal("§6=== Premium Battle Pass Info ==="));
        
        if (CobblePass.config.isSeasonActive()) {
            player.sendSystemMessage(Component.literal("§3Current Season: §b" + CobblePass.config.getCurrentSeason()));
            player.sendSystemMessage(Component.literal("§3Time Remaining: §b" + 
                formatTimeRemaining(CobblePass.config.getSeasonEndTime() - System.currentTimeMillis())));
            player.sendSystemMessage(Component.literal("§3Your Status: §b" + 
                (CobblePass.battlePass.getPlayerPass(player).hasPremium() ? "Premium" : "Free")));
            player.sendSystemMessage(Component.literal("§3Get premium with: §b/battlepass premium buy"));
        } else {
            player.sendSystemMessage(Component.literal("§cNo battle pass season is currently active!"));
            player.sendSystemMessage(Component.literal("§3Start a new season with: §b/battlepass start"));
        }

        return 1;
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
