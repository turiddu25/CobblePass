package com.cobblemon.mdks.cobblepass.util;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;

/**
 * Registry for managing and registering commands
 */
public abstract class CommandsRegistry {
    private static ArrayList<BaseCommand> commands = new ArrayList<>();

    /**
     * Add a command to the registry
     */
    public static void addCommand(BaseCommand command) {
        commands.add(command);
    }

    /**
     * Register all commands with the dispatcher
     */
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (BaseCommand command : commands) {
            command.register(dispatcher);
        }
    }

    /**
     * Register commands for Fabric/Forge compatibility
     */
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, 
                                      CommandBuildContext commandBuildContext,
                                      Commands.CommandSelection commandSelection) {
        registerCommands(dispatcher);
    }
}
