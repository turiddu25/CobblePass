package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SeasonCommand extends Subcommand {

    public SeasonCommand() {
        super("§9Usage:\n§3- /battlepass season <start|stop>");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("season")
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .then(Commands.literal("start")
                        .executes(this::startSeason))
                .then(Commands.literal("stop")
                        .executes(this::stopSeason))
                .build();
    }

    private int startSeason(CommandContext<CommandSourceStack> context) {
        if (CobblePass.config.isSeasonActive()) {
            context.getSource().sendFailure(Component.literal("A season is already active."));
            return 0;
        }

        CobblePass.config.startNewSeason();
        context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully started a new battle pass season!"), false);

        return 1;
    }

    private int stopSeason(CommandContext<CommandSourceStack> context) {
        if (!CobblePass.config.isSeasonActive()) {
            context.getSource().sendFailure(Component.literal("There is no active season to stop."));
            return 0;
        }

        CobblePass.config.stopSeason();
        context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully stopped the battle pass season."), false);

        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        // This command has subcommands, so this method shouldn't be called directly.
        return 0;
    }
}