package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.BattlePass;
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
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .executes(this::run)
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        try {
            // Reload configuration
            CobblePass.reload();
            
            // Reload battle pass data
            CobblePass.battlePass = new BattlePass(); // This will reload tiers through TierConfig

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
