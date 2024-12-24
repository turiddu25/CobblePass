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

    private int buyPremium(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.literal("This command must be run by a player"));
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();

        // Check if player already has premium
        if (CobblePass.battlePass.getPlayerPass(player).hasPremium()) {
            player.sendSystemMessage(Component.literal("§cYou already have premium battle pass!"));
            return 1;
        }

        // Grant premium
        CobblePass.battlePass.getPlayerPass(player).setPremium(true, CobblePass.config.getPremiumDuration());
        player.sendSystemMessage(Component.literal("§aSuccessfully granted premium battle pass!"));
        
        // Show duration info
        long days = CobblePass.config.getPremiumDuration() / (1000 * 60 * 60 * 24);
        if (days > 0) {
            player.sendSystemMessage(Component.literal(
                String.format("§aYour premium status will last for %d days", days)
            ));
        } else {
            player.sendSystemMessage(Component.literal("§aYour premium status is permanent"));
        }

        return 1;
    }

    private int showInfo(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.literal("This command must be run by a player"));
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        long days = CobblePass.config.getPremiumDuration() / (1000 * 60 * 60 * 24);

        player.sendSystemMessage(Component.literal("§6=== Premium Battle Pass Info ==="));
        if (days > 0) {
            player.sendSystemMessage(Component.literal(
                String.format("§3Duration: §b%d days", days)
            ));
        } else {
            player.sendSystemMessage(Component.literal("§3Duration: §bPermanent"));
        }
        player.sendSystemMessage(Component.literal(
            "§3Get premium with: §b/battlepass premium buy"
        ));

        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return showInfo(context);
    }
}
