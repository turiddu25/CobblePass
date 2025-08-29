package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.premium.PremiumManager;
import com.cobblemon.mdks.cobblepass.premium.PremiumMode;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.EconomyUtils;
import com.cobblemon.mdks.cobblepass.util.LangManager;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.cobblemon.mdks.cobblepass.util.Utils;
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
            context.getSource().sendSystemMessage(LangManager.get("lang.command.must_be_player"));
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        PremiumMode currentMode = PremiumManager.getInstance().getCurrentMode();

        // Check if premium purchasing is available in current mode
        if (currentMode == PremiumMode.DISABLED) {
            player.sendSystemMessage(Component.literal("§cPremium mode is disabled. All players have premium access automatically."));
            return 1;
        }

        if (currentMode == PremiumMode.PERMISSION) {
            player.sendSystemMessage(Component.literal("§cPremium access is managed through permissions. Contact an administrator."));
            return 1;
        }

        if (currentMode != PremiumMode.ECONOMY) {
            player.sendSystemMessage(Component.literal("§cPremium purchasing is not available in the current mode."));
            return 1;
        }

        // Check if season is active
        if (!CobblePass.config.isSeasonActive()) {
            player.sendSystemMessage(LangManager.get("lang.season.no_active"));
            return 1;
        }

        // Check if player already has premium
        if (PremiumManager.getInstance().hasPremium(player)) {
            player.sendSystemMessage(Component.literal("§cYou already have premium battle pass for this season!"));
            return 1;
        }

        // Check balance and charge for premium
        long cost = CobblePass.config.getPremiumCost();
        if (!EconomyUtils.hasBalance(player.getUUID(), cost)) {
            player.sendSystemMessage(Component.literal("§cYou need " + EconomyUtils.formatCurrency(cost) + 
                " to purchase premium battle pass!"));
            return 1;
        }

        // Deduct funds
        if (!EconomyUtils.withdraw(player.getUUID(), cost)) {
            player.sendSystemMessage(Component.literal("§cFailed to process payment!"));
            return 1;
        }

        // Grant premium through the provider system
        boolean success = PremiumManager.getInstance().grantPremium(player);
        
        if (success) {
            // Save player data
            CobblePass.battlePass.savePlayerPass(player.getUUID().toString());
            
            player.sendSystemMessage(Component.literal("§aSuccessfully purchased premium battle pass for Season " + 
                CobblePass.config.getCurrentSeason() + " for " + EconomyUtils.formatCurrency(cost) + "!"));
        } else {
            // Refund if premium grant failed
            // TODO: Implement deposit method in EconomyUtils for refunds
            // EconomyUtils.deposit(player.getUUID(), cost);
            player.sendSystemMessage(Component.literal("§cFailed to grant premium access. Refund functionality not yet implemented."));
        }

        return success ? 1 : 0;
    }

    private int showInfo(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(LangManager.get("lang.command.must_be_player"));
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        PremiumMode currentMode = PremiumManager.getInstance().getCurrentMode();
        
        player.sendSystemMessage(Component.literal("§6=== Premium Battle Pass Info ==="));
        player.sendSystemMessage(Component.literal("§3Premium Mode: §b" + currentMode.getConfigValue()));
        player.sendSystemMessage(Component.literal("§3Mode Description: §7" + currentMode.getDescription()));
        
        if (CobblePass.config.isSeasonActive()) {
            player.sendSystemMessage(Component.literal("§3Current Season: §b" + CobblePass.config.getCurrentSeason()));
            player.sendSystemMessage(Component.literal("§3Time Remaining: §b" + 
                formatTimeRemaining(CobblePass.config.getSeasonEndTime() - System.currentTimeMillis())));
            
            boolean hasPremium = PremiumManager.getInstance().hasPremium(player);
            String statusMessage = PremiumManager.getInstance().getStatusMessage(player);
            
            player.sendSystemMessage(Component.literal("§3Your Status: §b" + (hasPremium ? "Premium" : "Free")));
            player.sendSystemMessage(Component.literal("§3Details: §7" + statusMessage));
            
            // Show appropriate action based on current mode
            switch (currentMode) {
                case ECONOMY:
                    if (!hasPremium) {
                        player.sendSystemMessage(Component.literal("§3Get premium with: §b/battlepass premium buy"));
                        player.sendSystemMessage(Component.literal("§3Cost: §b" + EconomyUtils.formatCurrency(CobblePass.config.getPremiumCost())));
                    }
                    break;
                case PERMISSION:
                    if (!hasPremium) {
                        player.sendSystemMessage(Component.literal("§3Contact an administrator for premium access"));
                        player.sendSystemMessage(Component.literal("§3Required permission: §b" + CobblePass.config.getPremiumConfig().getPermissionNode()));
                    }
                    break;
                case DISABLED:
                    player.sendSystemMessage(Component.literal("§3All players have premium access in this mode"));
                    break;
            }
        } else {
            player.sendSystemMessage(LangManager.get("lang.season.no_active"));
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
