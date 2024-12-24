package com.cobblemon.mdks.cobblepass.util;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all commands in the battle pass system
 */
public abstract class BaseCommand {
    // The command name
    private String commandString;
    // Command aliases
    private ArrayList<String> aliases;
    // Required permission
    private CobblemonPermission permission;
    // Subcommands
    private ArrayList<Subcommand> subcommands;

    /**
     * Constructor for base command
     * @param commandString The primary command string
     * @param aliases List of command aliases
     * @param permission Required permission to use the command
     * @param subcommands List of subcommands
     */
    public BaseCommand(String commandString, List<String> aliases, CobblemonPermission permission,
                      List<Subcommand> subcommands) {
        this.commandString = commandString;
        this.aliases = new ArrayList<>(aliases);
        this.permission = permission;
        this.subcommands = new ArrayList<>(subcommands);
    }

    /**
     * Register the command and its subcommands
     */
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Create the base command
        LiteralCommandNode<CommandSourceStack> root = Commands
                .literal(commandString)
                .requires(ctx -> {
                    if (CobblePass.config.isEnablePermissionNodes()) {
                        if (ctx.isPlayer()) {
                            return CobblePass.permissions.hasPermission(ctx.getPlayer(), permission);
                        } else {
                            return true;
                        }
                    }
                    return true;
                })
                .executes(this::run)
                .build();

        // Register the command
        dispatcher.getRoot().addChild(root);

        // Register aliases
        for (String alias : aliases) {
            dispatcher.register(Commands.literal(alias).redirect(root).executes(this::run));
        }

        // Register subcommands
        for (Subcommand subcommand : subcommands) {
            root.addChild(subcommand.build());
        }
    }

    /**
     * Execute the command
     * @param context Command context
     * @return Command result
     */
    public abstract int run(CommandContext<CommandSourceStack> context);
}
