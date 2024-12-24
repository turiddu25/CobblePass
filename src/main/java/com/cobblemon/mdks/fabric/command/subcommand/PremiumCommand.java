package com.cobblemon.mdks.fabric.command.subcommand;

import com.cobblemon.mdks.fabric.CobblePass;
import com.cobblemon.mdks.fabric.util.Subcommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigDecimal;

public class PremiumCommand extends Subcommand {

    private final EconomyService economyService = EconomyService.instance();

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
        Account account = economyService.account(player.getUUID()).join();

        // Check if player already has premium
        if (CobblePass.battlePass.getPlayerPass(player).hasPremium()) {
            player.sendSystemMessage(Component.literal("§cYou already have premium battle pass!"));
            return 1;
        }

        // Check if player has enough money
        double cost = CobblePass.config.getPremiumCost();
        if (account.balance().doubleValue() < cost) {
            player.sendSystemMessage(Component.literal(
                String.format("§cYou need $%.2f to purchase premium battle pass!", cost)
            ));
            return 1;
        }

        // Withdraw money and grant premium
        boolean success = account.withdraw(BigDecimal.valueOf(cost)).successful();
        if (success) {
            CobblePass.battlePass.getPlayerPass(player).setPremium(true, CobblePass.config.getPremiumDuration());
            player.sendSystemMessage(Component.literal("§aSuccessfully purchased premium battle pass!"));
            
            // Show duration info
            long days = CobblePass.config.getPremiumDuration() / (1000 * 60 * 60 * 24);
            if (days > 0) {
                player.sendSystemMessage(Component.literal(
                    String.format("§aYour premium status will last for %d days", days)
                ));
            } else {
                player.sendSystemMessage(Component.literal("§aYour premium status is permanent"));
            }
        } else {
            player.sendSystemMessage(Component.literal("§cFailed to purchase premium battle pass!"));
        }

        return 1;
    }

    private int showInfo(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.literal("This command must be run by a player"));
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        double cost = CobblePass.config.getPremiumCost();
        long days = CobblePass.config.getPremiumDuration() / (1000 * 60 * 60 * 24);

        player.sendSystemMessage(Component.literal("§6=== Premium Battle Pass Info ==="));
        player.sendSystemMessage(Component.literal(
            String.format("§3Cost: §b$%.2f", cost)
        ));
        if (days > 0) {
            player.sendSystemMessage(Component.literal(
                String.format("§3Duration: §b%d days", days)
            ));
        } else {
            player.sendSystemMessage(Component.literal("§3Duration: §bPermanent"));
        }
        player.sendSystemMessage(Component.literal(
            "§3Purchase with: §b/battlepass premium buy"
        ));

        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return showInfo(context);
    }
}
