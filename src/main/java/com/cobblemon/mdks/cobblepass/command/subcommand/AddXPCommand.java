package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AddXPCommand extends Subcommand {

    public AddXPCommand() {
        super("§9Usage:\n§3- /battlepass addxp <player> <amount>");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("addxp")
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(this::run)))
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "player");
            int amount = IntegerArgumentType.getInteger(context, "amount");

            CobblePass.battlePass.addXP(target, amount);

            // Notify both command sender and target
            context.getSource().sendSystemMessage(Component.literal(
                String.format("§aAdded %d XP to %s's battle pass", 
                    amount, target.getName().getString())
            ));

            if (context.getSource().getPlayer() != target) {
                target.sendSystemMessage(Component.literal(
                    String.format("§aReceived %d battle pass XP!", amount)
                ));
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendSystemMessage(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }
}
