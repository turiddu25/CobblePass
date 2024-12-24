package com.cobblemon.mdks.fabric.command;

import com.cobblemon.mdks.fabric.command.subcommand.*;
import com.cobblemon.mdks.fabric.util.BaseCommand;
import com.cobblemon.mdks.fabric.util.Constants;
import com.cobblemon.mdks.fabric.util.Permissions;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

public class BattlePassCommand extends BaseCommand {
    public BattlePassCommand() {
        super("battlepass",
                Constants.COMMAND_ALIASES,
                new Permissions().getPermission(Constants.PERM_COMMAND_BASE),
                Arrays.asList(
                        new ViewCommand(),
                        new ClaimCommand(),
                        new AddXPCommand(),
                        new PremiumCommand(),
                        new ReloadCommand()
                )
        );
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
        ViewCommand.showBattlePassInfo(player);
        return 1;
    }
}
