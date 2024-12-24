package com.cobblemon.mdks.fabric.command.subcommand;

import com.cobblemon.mdks.fabric.CobblePass;
import com.cobblemon.mdks.fabric.battlepass.BattlePassTier;
import com.cobblemon.mdks.fabric.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.fabric.util.Constants;
import com.cobblemon.mdks.fabric.util.Subcommand;
import com.cobblemon.mdks.fabric.util.Utils;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ViewCommand extends Subcommand {
    public ViewCommand() {
        super("§9Usage: §3/battlepass view");
    }

    @Override
    public CommandNode<CommandSourceStack> build() {
        return Commands.literal("view")
            .executes(this::run)
            .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(
                Component.literal(Constants.ERROR_PREFIX + "This command must be run by a player!")
            );
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        showBattlePassInfo(player);
        return 1;
    }

    public static void showBattlePassInfo(ServerPlayer player) {
        PlayerBattlePass pass = CobblePass.battlePass.getPlayerPass(player);
        
        // Header
        player.sendSystemMessage(Component.literal("§b=== Battle Pass Info ==="));
        
        // Level and XP
        int currentXP = pass.getXP();
        int xpForNext = (int)(CobblePass.config.getXpPerLevel() * Math.pow(Constants.XP_MULTIPLIER, pass.getLevel() - 1));
        player.sendSystemMessage(Component.literal(String.format(
            "§3Level: §f%d §7(§f%d§7/§f%d XP§7)",
            pass.getLevel(), currentXP, xpForNext
        )));

        // Premium Status
        if (pass.isPremium()) {
            if (pass.getPremiumExpiry() > 0) {
                long timeLeft = pass.getPremiumExpiry() - System.currentTimeMillis();
                player.sendSystemMessage(Component.literal(
                    "§6Premium Status: §aActive §7(Expires in: " + Utils.formatDuration(timeLeft) + ")"
                ));
            } else {
                player.sendSystemMessage(Component.literal("§6Premium Status: §aActive §7(Permanent)"));
            }
        } else {
            player.sendSystemMessage(Component.literal("§6Premium Status: §cInactive"));
        }

        // Available Rewards
        player.sendSystemMessage(Component.literal("\n§b=== Available Rewards ==="));
        
        for (BattlePassTier tier : CobblePass.battlePass.getTiers()) {
            if (tier.getLevel() > pass.getLevel()) continue;

            StringBuilder rewardInfo = new StringBuilder("§3Level " + tier.getLevel() + ": ");

            // Free reward
            if (tier.hasFreeReward()) {
                if (pass.hasClaimedFreeReward(tier.getLevel())) {
                    rewardInfo.append("§7[Claimed] ");
                } else {
                    rewardInfo.append("§a[Unclaimed] ");
                }
                rewardInfo.append("§f").append(tier.getFreeRewardItem().getDisplayName().getString());
            }

            // Premium reward
            if (tier.hasPremiumReward()) {
                rewardInfo.append(" §7| ");
                if (!pass.isPremium()) {
                    rewardInfo.append("§c[Premium Only] ");
                } else if (pass.hasClaimedPremiumReward(tier.getLevel())) {
                    rewardInfo.append("§7[Claimed] ");
                } else {
                    rewardInfo.append("§6[Premium Unclaimed] ");
                }
                rewardInfo.append("§f").append(tier.getPremiumRewardItem().getDisplayName().getString());
            }

            player.sendSystemMessage(Component.literal(rewardInfo.toString()));
        }

        // Footer with command help
        player.sendSystemMessage(Component.literal("\n§7Use §f/battlepass claim <level> §7to claim rewards"));
        if (!pass.isPremium()) {
            player.sendSystemMessage(Component.literal("§7Use §f/battlepass premium §7to unlock premium rewards"));
        }
    }
}
