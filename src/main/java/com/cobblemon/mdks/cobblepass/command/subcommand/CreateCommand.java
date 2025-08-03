package com.cobblemon.mdks.cobblepass.command.subcommand;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class CreateCommand extends Subcommand {

    private int duration = 30;
    private int maxLevel = 10;
    private boolean premium = false;

    public CreateCommand() {
        super("§9Usage:\n§3- /battlepass create");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("create")
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .executes(this::run)
                .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(Component.literal("This command must be run by a player."));
            return 1;
        }

        java.io.File configFile = new java.io.File(com.cobblemon.mdks.cobblepass.util.Constants.CONFIG_DIR, com.cobblemon.mdks.cobblepass.util.Constants.CONFIG_FILE);
        java.io.File tiersFile = new java.io.File(com.cobblemon.mdks.cobblepass.util.Constants.CONFIG_DIR, com.cobblemon.mdks.cobblepass.util.Constants.TIERS_FILE);

        if (configFile.exists() || tiersFile.exists()) {
            context.getSource().sendFailure(Component.literal("A battle pass already exists. Please use /battlepass delete to remove it first."));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayer();
        openCreationGUI(player);
        return 1;
    }

    private void openCreationGUI(ServerPlayer player) {
        List<Component> durationLore = new ArrayList<>();
        durationLore.add(Component.literal("§7Currently: §f" + duration + " days"));

        Button durationButton = GooeyButton.builder()
                .display(new ItemStack(Items.CLOCK))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§eSet Duration (Days)"))
                .with(DataComponents.LORE, new ItemLore(durationLore))
                .onClick((action) -> openDurationGUI(player))
                .build();

        List<Component> levelsLore = new ArrayList<>();
        levelsLore.add(Component.literal("§7Currently: §f" + maxLevel + " levels"));

        Button levelsButton = GooeyButton.builder()
                .display(new ItemStack(Items.EXPERIENCE_BOTTLE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§eSet Max Level"))
                .with(DataComponents.LORE, new ItemLore(levelsLore))
                .onClick((action) -> openLevelsGUI(player))
                .build();

        List<Component> premiumLore = new ArrayList<>();
        premiumLore.add(Component.literal("§7Currently: §f" + (premium ? "Enabled" : "Disabled")));

        Button premiumButton = GooeyButton.builder()
                .display(new ItemStack(Items.GOLD_INGOT))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§eToggle Premium"))
                .with(DataComponents.LORE, new ItemLore(premiumLore))
                .onClick((action) -> {
                    premium = !premium;
                    openCreationGUI(player); // Refresh the GUI
                })
                .build();

        Button createButton = GooeyButton.builder()
                .display(new ItemStack(Items.GREEN_WOOL))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§aCreate Battle Pass"))
                .onClick((action) -> {
                    // Wipes all previous player progress to ensure a clean start for the new pass
                    CobblePass.battlePass.resetAllPlayerData();

                    if (CobblePass.config == null) {
                        CobblePass.config = new com.cobblemon.mdks.cobblepass.config.Config();
                    }
                    if (CobblePass.battlePass == null) {
                        CobblePass.battlePass = new com.cobblemon.mdks.cobblepass.battlepass.BattlePass();
                    }
                    CobblePass.config.createNewSeason(duration, maxLevel, premium);
                    CobblePass.battlePass.generateNewTiers(maxLevel);
                    player.closeContainer();
                    player.sendSystemMessage(Component.literal("§aBattle Pass created successfully!"));
                    player.sendSystemMessage(Component.literal("§eA new tiers.json has been generated with placeholder rewards."));
                    player.sendSystemMessage(Component.literal("§eUse /battlepass season start to begin the season."));
                })
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .set(1, 1, durationButton)
                .set(1, 3, levelsButton)
                .set(1, 5, premiumButton)
                .set(1, 7, createButton)
                .build();

        LinkedPage page = LinkedPage.builder()
                .title("§3Create Battle Pass")
                .template(template)
                .build();

        UIManager.openUIForcefully(player, page);
    }

    private void openDurationGUI(ServerPlayer player) {
        Button plusButton = GooeyButton.builder()
                .display(new ItemStack(Items.GREEN_STAINED_GLASS_PANE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§a+1 Day"))
                .onClick((action) -> {
                    duration++;
                    openDurationGUI(player);
                })
                .build();

        Button minusButton = GooeyButton.builder()
                .display(new ItemStack(Items.RED_STAINED_GLASS_PANE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§c-1 Day"))
                .onClick((action) -> {
                    if (duration > 1) {
                        duration--;
                    }
                    openDurationGUI(player);
                })
                .build();

        Button backButton = GooeyButton.builder()
                .display(new ItemStack(Items.BARRIER))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§cBack"))
                .onClick((action) -> openCreationGUI(player))
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .set(1, 2, minusButton)
                .set(1, 4, GooeyButton.builder().display(new ItemStack(Items.CLOCK)).with(DataComponents.CUSTOM_NAME, Component.literal("§eDuration: " + duration + " days")).build())
                .set(1, 6, plusButton)
                .set(2, 8, backButton)
                .build();

        LinkedPage page = LinkedPage.builder()
                .title("§3Set Duration")
                .template(template)
                .build();

        UIManager.openUIForcefully(player, page);
    }

    private void openLevelsGUI(ServerPlayer player) {
        Button plusButton = GooeyButton.builder()
                .display(new ItemStack(Items.GREEN_STAINED_GLASS_PANE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§a+1 Level"))
                .onClick((action) -> {
                    maxLevel++;
                    openLevelsGUI(player);
                })
                .build();

        Button minusButton = GooeyButton.builder()
                .display(new ItemStack(Items.RED_STAINED_GLASS_PANE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§c-1 Level"))
                .onClick((action) -> {
                    if (maxLevel > 1) {
                        maxLevel--;
                    }
                    openLevelsGUI(player);
                })
                .build();

        Button backButton = GooeyButton.builder()
                .display(new ItemStack(Items.BARRIER))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§cBack"))
                .onClick((action) -> openCreationGUI(player))
                .build();

        ChestTemplate template = ChestTemplate.builder(3)
                .set(1, 2, minusButton)
                .set(1, 4, GooeyButton.builder().display(new ItemStack(Items.EXPERIENCE_BOTTLE)).with(DataComponents.CUSTOM_NAME, Component.literal("§eMax Level: " + maxLevel)).build())
                .set(1, 6, plusButton)
                .set(2, 8, backButton)
                .build();

        LinkedPage page = LinkedPage.builder()
                .title("§3Set Max Level")
                .template(template)
                .build();

        UIManager.openUIForcefully(player, page);
    }
}