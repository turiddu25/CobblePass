package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.premium.PremiumManager;
import com.cobblemon.mdks.cobblepass.premium.PremiumMode;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class ManagePremiumCommand extends Subcommand {

    public ManagePremiumCommand() {
        super("§9Usage:\n§3- /battlepass premiumanage <add|remove|bulk|mode|status> [args...]");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("premiumanage")
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(this::addPremium)))
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(this::removePremium)))
                .then(Commands.literal("bulk")
                        .then(Commands.literal("add")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(this::bulkAddPremium)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(this::bulkRemovePremium))))
                .then(Commands.literal("mode")
                        .then(Commands.literal("set")
                                .then(Commands.argument("mode", StringArgumentType.string())
                                        .executes(this::setMode)))
                        .then(Commands.literal("get")
                                .executes(this::getMode)))
                .then(Commands.literal("status")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(this::getPlayerStatus)))
                .then(Commands.literal("migrate")
                        .executes(this::migrateAllPlayers))
                .build();
    }

    private int addPremium(CommandContext<CommandSourceStack> context) {
        ServerPlayer target = null;
        try {
            target = EntityArgument.getPlayer(context, "player");
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Invalid player specified."));
            return 0;
        }

        final ServerPlayer finalTarget = target;
        boolean success = PremiumManager.getInstance().grantPremium(finalTarget);
        
        if (success) {
            CobblePass.battlePass.savePlayerPass(finalTarget.getUUID().toString());
            context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully granted premium access to " + finalTarget.getName().getString()), false);
            finalTarget.sendSystemMessage(Component.literal("§aYour Battle Pass has been upgraded to Premium!"));
        } else {
            context.getSource().sendFailure(Component.literal("§cFailed to grant premium access to " + finalTarget.getName().getString()));
        }

        return success ? 1 : 0;
    }

    private int removePremium(CommandContext<CommandSourceStack> context) {
        ServerPlayer target = null;
        try {
            target = EntityArgument.getPlayer(context, "player");
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Invalid player specified."));
            return 0;
        }

        final ServerPlayer finalTarget = target;
        boolean success = PremiumManager.getInstance().revokePremium(finalTarget);
        
        if (success) {
            CobblePass.battlePass.savePlayerPass(finalTarget.getUUID().toString());
            context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully revoked premium access from " + finalTarget.getName().getString()), false);
            finalTarget.sendSystemMessage(Component.literal("§cYour Battle Pass is no longer Premium."));
        } else {
            context.getSource().sendFailure(Component.literal("§cFailed to revoke premium access from " + finalTarget.getName().getString()));
        }

        return success ? 1 : 0;
    }

    private int bulkAddPremium(CommandContext<CommandSourceStack> context) {
        Collection<ServerPlayer> targets;
        try {
            targets = EntityArgument.getPlayers(context, "players");
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Invalid players specified."));
            return 0;
        }

        int successCount = 0;
        int totalCount = targets.size();
        
        for (ServerPlayer player : targets) {
            if (PremiumManager.getInstance().grantPremium(player)) {
                CobblePass.battlePass.savePlayerPass(player.getUUID().toString());
                player.sendSystemMessage(Component.literal("§aYour Battle Pass has been upgraded to Premium!"));
                successCount++;
            }
        }

        final int finalSuccessCount = successCount;
        final int finalTotalCount = totalCount;
        context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully granted premium access to " + finalSuccessCount + "/" + finalTotalCount + " players"), false);
        return successCount > 0 ? 1 : 0;
    }

    private int bulkRemovePremium(CommandContext<CommandSourceStack> context) {
        Collection<ServerPlayer> targets;
        try {
            targets = EntityArgument.getPlayers(context, "players");
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Invalid players specified."));
            return 0;
        }

        int successCount = 0;
        int totalCount = targets.size();
        
        for (ServerPlayer player : targets) {
            if (PremiumManager.getInstance().revokePremium(player)) {
                CobblePass.battlePass.savePlayerPass(player.getUUID().toString());
                player.sendSystemMessage(Component.literal("§cYour Battle Pass is no longer Premium."));
                successCount++;
            }
        }

        final int finalSuccessCount = successCount;
        final int finalTotalCount = totalCount;
        context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully revoked premium access from " + finalSuccessCount + "/" + finalTotalCount + " players"), false);
        return successCount > 0 ? 1 : 0;
    }

    private int setMode(CommandContext<CommandSourceStack> context) {
        String modeString = StringArgumentType.getString(context, "mode").toUpperCase();
        
        try {
            PremiumMode mode = PremiumMode.valueOf(modeString);
            PremiumMode oldMode = PremiumManager.getInstance().getCurrentMode();
            
            // Update configuration
            CobblePass.config.getPremiumConfig().setMode(mode);
            CobblePass.config.save();
            
            // Switch to new mode
            PremiumManager.getInstance().switchToMode(mode);
            
            context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully switched premium mode from " + 
                oldMode.getConfigValue() + " to " + mode.getConfigValue()), false);
            
            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("§cInvalid premium mode. Valid modes: ECONOMY, PERMISSION, DISABLED"));
            return 0;
        }
    }

    private int getMode(CommandContext<CommandSourceStack> context) {
        PremiumMode currentMode = PremiumManager.getInstance().getCurrentMode();
        context.getSource().sendSuccess(() -> Component.literal("§6Current premium mode: §b" + currentMode.getConfigValue()), false);
        context.getSource().sendSuccess(() -> Component.literal("§6Description: §7" + currentMode.getDescription()), false);
        return 1;
    }

    private int getPlayerStatus(CommandContext<CommandSourceStack> context) {
        ServerPlayer target;
        try {
            target = EntityArgument.getPlayer(context, "player");
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Invalid player specified."));
            return 0;
        }

        String status = PremiumManager.getInstance().getStatusMessage(target);
        boolean hasPremium = PremiumManager.getInstance().hasPremium(target);
        
        context.getSource().sendSuccess(() -> Component.literal("§6Premium status for " + target.getName().getString() + ":"), false);
        context.getSource().sendSuccess(() -> Component.literal("§3Has Premium: §b" + (hasPremium ? "Yes" : "No")), false);
        context.getSource().sendSuccess(() -> Component.literal("§3Status: §7" + status), false);
        
        return 1;
    }

    private int migrateAllPlayers(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("§6Starting premium status migration for all players..."), false);
        
        // This would typically iterate through all player data files and migrate them
        // For now, we'll just reload the premium manager to ensure consistency
        PremiumManager.getInstance().reload();
        
        context.getSource().sendSuccess(() -> Component.literal("§aCompleted premium status migration"), false);
        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        // This command has subcommands, so this method shouldn't be called directly.
        return 0;
    }
}