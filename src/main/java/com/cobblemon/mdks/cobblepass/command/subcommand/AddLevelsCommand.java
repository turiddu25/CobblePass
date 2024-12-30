package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
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
            int currentLevel = CobblePass.battlePass.getPlayerPass(target).getLevel();
            
            // Ensure we don't exceed max level
            if (currentLevel + levelsToAdd > 100) {
                levelsToAdd = 100 - currentLevel;
                if (levelsToAdd <= 0) {
                    context.getSource().sendSystemMessage(Component.literal("§cError: Player is already at max level"));
                    return 0;
                }
            }

            // Calculate total XP needed for target level
            int totalXpNeeded = 0;
            double baseXp = CobblePass.config.getXpPerLevel();
            for (int i = 0; i < levelsToAdd; i++) {
                totalXpNeeded += (int)(baseXp * Math.pow(Constants.XP_MULTIPLIER, currentLevel + i - 1));
            }

            CobblePass.battlePass.addXP(target, totalXpNeeded);

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
