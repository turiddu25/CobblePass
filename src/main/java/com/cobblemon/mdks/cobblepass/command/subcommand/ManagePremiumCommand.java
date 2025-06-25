package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ManagePremiumCommand extends Subcommand {

    public ManagePremiumCommand() {
        super("§9Usage:\n§3- /battlepass premium <add|remove> <player>");
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
        PlayerBattlePass pass = CobblePass.battlePass.getPlayerPass(finalTarget);
        pass.setPremium(true);
        CobblePass.battlePass.savePlayerPass(finalTarget.getUUID().toString());

        context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully added premium status to " + finalTarget.getName().getString()), false);
        finalTarget.sendSystemMessage(Component.literal("§aYour Battle Pass has been upgraded to Premium!"));

        return 1;
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
        PlayerBattlePass pass = CobblePass.battlePass.getPlayerPass(finalTarget);
        pass.setPremium(false);
        CobblePass.battlePass.savePlayerPass(finalTarget.getUUID().toString());

        context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully removed premium status from " + finalTarget.getName().getString()), false);
        finalTarget.sendSystemMessage(Component.literal("§cYour Battle Pass is no longer Premium."));

        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        // This command has subcommands, so this method shouldn't be called directly.
        return 0;
    }
}