package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadCommand extends Subcommand {

    public ReloadCommand() {
        super("§9Usage:\n§3- /battlepass reload");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("reload")
                .requires(source -> {
                    if (CobblePass.config.isEnablePermissionNodes()) {
                        return source.hasPermission(2) && // Op level 2 minimum
                               (!source.isPlayer() || 
                                CobblePass.permissions.hasPermission(
                                    source.getPlayer(),
                                    "cobblepass.admin.reload"
                                ));
                    }
                    return source.hasPermission(2);
                })
                .executes(this::run)
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        try {
            // Reload configuration
            CobblePass.reload();
            
            // Reload battle pass data
            CobblePass.battlePass.loadTiers();

            context.getSource().sendSystemMessage(Component.literal(
                "§aBattle Pass configuration reloaded successfully!"
            ));

            // Log reload event
            CobblePass.LOGGER.info("Battle Pass configuration reloaded by " + 
                (context.getSource().isPlayer() ? 
                    context.getSource().getPlayer().getName().getString() : 
                    "Console"
                ));

            return 1;
        } catch (Exception e) {
            String errorMessage = "§cFailed to reload Battle Pass configuration: " + e.getMessage();
            context.getSource().sendSystemMessage(Component.literal(errorMessage));
            
            // Log error
            CobblePass.LOGGER.error("Failed to reload Battle Pass configuration:");
            e.printStackTrace();
            
            return 0;
        }
    }
}
