package com.cobblemon.mdks.cobblepass.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Base class for all subcommands in the battle pass system
 */
public abstract class Subcommand {
    // Usage message for the command
    private String usage;

    /**
     * Constructor for subcommand
     * @param usageString The usage message to display
     */
    public Subcommand(String usageString) {
        this.usage = usageString;
    }

    /**
     * Show the usage message to the command sender
     * @param context Command context
     * @return Command result
     */
    public int showUsage(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal(
            formatMessage(usage, context.getSource().isPlayer())
        ));
        return 1;
    }

    /**
     * Format a message based on whether it's being sent to a player or console
     * @param message The message to format
     * @param isPlayer Whether the recipient is a player
     * @return Formatted message
     */
    protected String formatMessage(String message, boolean isPlayer) {
        if (isPlayer) {
            return message.trim();
        } else {
            // Remove color codes for console
            return message.replaceAll("§[0-9a-fk-or]", "").trim();
        }
    }

    /**
     * Build the command node for this subcommand
     * @return Command node
     */
    public abstract CommandNode<CommandSourceStack> build();

    /**
     * Execute the subcommand
     * @param context Command context
     * @return Command result
     */
    public abstract int run(CommandContext<CommandSourceStack> context);
}
