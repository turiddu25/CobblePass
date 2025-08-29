package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.config.XpProgression;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AddLevelsCommand extends Subcommand {

    public AddLevelsCommand() {
        super("§9Usage:\n§3- /battlepass addlevels [player] <levels>");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("addlevels")
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("levels", IntegerArgumentType.integer(1, 100))
                        .executes(this::run)))
                .then(Commands.argument("levels", IntegerArgumentType.integer(1, 100))
                    .executes(this::run))
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target;
            try {
                target = EntityArgument.getPlayer(context, "player");
            } catch (IllegalArgumentException e) {
                target = context.getSource().getPlayer();
            }

            if (target == null) {
                context.getSource().sendSystemMessage(Component.literal("§cError: Player not found"));
                return 0;
            }

            int levelsToAdd = IntegerArgumentType.getInteger(context, "levels");
            com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass playerPass = CobblePass.battlePass.getPlayerPass(target);
            int currentLevel = playerPass.getLevel();
            int currentXp = playerPass.getXP();
            int maxLevel = CobblePass.config.getMaxLevel();

            if (currentLevel >= maxLevel) {
                context.getSource().sendSystemMessage(Component.literal("§cError: Player is already at max level"));
                return 0;
            }

            if (currentLevel + levelsToAdd > maxLevel) {
                levelsToAdd = maxLevel - currentLevel;
            }

            XpProgression xpProgression = CobblePass.config.getXpProgression();

            // This helper function gets the XP required to complete a given level, based on the player's current level.
            java.util.function.Function<Integer, Integer> getXpForLevelCompletion = (level) -> {
                if (xpProgression.getMode().equalsIgnoreCase("MANUAL")) {
                    return xpProgression.getManualXpForLevel(level + 1);
                } else {
                    return (int) (xpProgression.getXpPerLevel() * Math.pow(xpProgression.getXpMultiplier(), level - 1));
                }
            };

            // 1. Calculate XP needed to complete the player's current level.
            int xpToCompleteCurrentLevel = getXpForLevelCompletion.apply(currentLevel) - currentXp;
            int totalXpToAdd = xpToCompleteCurrentLevel;

            // 2. Add the full XP cost for the remaining levels to add.
            for (int i = 1; i < levelsToAdd; i++) {
                totalXpToAdd += getXpForLevelCompletion.apply(currentLevel + i);
            }

            CobblePass.battlePass.addXP(target, totalXpToAdd);

            // Notify both command sender and target
            String message = String.format("§aAdded %d levels to %s's battle pass", 
                levelsToAdd, target.getName().getString());
            context.getSource().sendSystemMessage(Component.literal(message));

            if (context.getSource().getPlayer() != target) {
                target.sendSystemMessage(Component.literal(
                    String.format("§aReceived %d battle pass levels!", levelsToAdd)
                ));
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendSystemMessage(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }
}
