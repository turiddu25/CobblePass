package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.File;

public class DeleteCommand extends Subcommand {

    public DeleteCommand() {
        super("§9Usage:\n§3- /battlepass delete");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("delete")
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .executes(this::confirm)
                .then(Commands.literal("confirm")
                        .executes(this::delete))
                .build();
    }

    private int confirm(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("§cAre you sure you want to delete the battle pass? This action cannot be undone. §7Run /battlepass delete confirm to proceed."), false);
        return 1;
    }

    private int delete(CommandContext<CommandSourceStack> context) {
        File configFile = new File(Constants.CONFIG_DIR, Constants.CONFIG_FILE);
        File tiersFile = new File(Constants.CONFIG_DIR, Constants.TIERS_FILE);
        File playersDir = new File(Constants.PLAYER_DATA_DIR);

        boolean success = true;

        if (configFile.exists() && !configFile.delete()) {
            success = false;
            context.getSource().sendFailure(Component.literal("§cFailed to delete config.json."));
        }
        if (tiersFile.exists() && !tiersFile.delete()) {
            success = false;
            context.getSource().sendFailure(Component.literal("§cFailed to delete tiers.json."));
        }

        // Robustly delete the entire player data directory
        if (playersDir.exists()) {
            try {
                java.nio.file.Files.walk(playersDir.toPath())
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(java.nio.file.Path::toFile)
                    .forEach(java.io.File::delete);
            } catch (java.io.IOException e) {
                success = false;
                context.getSource().sendFailure(Component.literal("§cFailed to delete player data directory: " + e.getMessage()));
            }
        }
        
        // This is the most important part: reset the data in memory
        CobblePass.resetInstance();

        if (success) {
            context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully deleted all battle pass files and data."), false);
        } else {
            context.getSource().sendFailure(Component.literal("§cCould not delete all files. A server restart may be required after manual deletion."));
        }
        
        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        // This command has subcommands, so this method shouldn't be called directly.
        return 0;
    }
}