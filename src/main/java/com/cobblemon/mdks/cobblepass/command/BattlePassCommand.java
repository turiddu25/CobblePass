package com.cobblemon.mdks.cobblepass.command;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.command.subcommand.AddLevelsCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.AddXPCommand;
import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mdks.cobblepass.battlepass.BattlePassTier;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.command.subcommand.ClaimCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.CreateCommand;
import com.cobblemon.mdks.cobblepass.data.Reward;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import com.cobblemon.mdks.cobblepass.command.subcommand.ManagePremiumCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.PremiumCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.ReloadCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.DeleteCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.SeasonCommand;
import com.cobblemon.mdks.cobblepass.util.BaseCommand;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Permissions;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BattlePassCommand extends BaseCommand {
    public BattlePassCommand() {
        super("battlepass",
                Constants.COMMAND_ALIASES,
                new Permissions().getPermission(Constants.PERM_COMMAND_BASE),
                List.of(
                        new ClaimCommand(),
                        new AddLevelsCommand(),
                        new PremiumCommand(),
                        new ReloadCommand(),
                        new CreateCommand(),
                        new AddXPCommand(),
                        new ManagePremiumCommand(),
                        new SeasonCommand(),
                        new DeleteCommand()
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
        if (!CobblePass.config.isSeasonActive()) {
            if (context.getSource().hasPermission(4)) {
                showBattlePassInfo(player);
            } else {
                context.getSource().sendSystemMessage(
                        Component.literal(Constants.MSG_NO_ACTIVE_SEASON)
                );
            }
            return 1;
        }

        showBattlePassInfo(player);
        return 1;
    }

    private static String formatTimeRemaining(long milliseconds) {
        long seconds = milliseconds / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        
        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
    }

    private static List<Component> getRewardLore(BattlePassTier tier, boolean isPremium) {
        List<Component> lore = new ArrayList<>();
        Reward reward = isPremium ? tier.getPremiumReward() : tier.getFreeReward();
        if (reward != null) {
            JsonObject data = reward.getData();
            switch (reward.getType()) {
                case ITEM:
                    if (data != null) {
                        String itemId = data.get("id").getAsString();
                        int count = data.has("Count") ? data.get("Count").getAsInt() : 1;
                        // Extract item name from ID (e.g., "minecraft:stone" -> "Stone")
                        String[] parts = itemId.split(":");
                        String itemName = parts[parts.length - 1];
                        itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
                        // Add mod name to lore if not minecraft
                        if (!parts[0].equals("minecraft")) {
                            String modName = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
                            lore.add(Component.literal("§8" + modName + " Item"));
                        }
                        lore.add(Component.literal("§7" + count + "x " + itemName));
                    } else {
                        lore.add(Component.literal("§7Item"));
                    }
                    break;
                case POKEMON:
                    lore.add(Component.literal("§7Pokemon"));
                    if (data != null) {
                        if (data.has("species")) {
                            lore.add(Component.literal(data.get("species").getAsString()));
                        }
                        if (data.has("level")) {
                            lore.add(Component.literal("§7Level: §f" + data.get("level").getAsInt()));
                        }
                        if (data.has("shiny") && data.get("shiny").getAsBoolean()) {
                            lore.add(Component.literal("§6✦ Shiny"));
                        }
                    }
                    break;
                case COMMAND:
                    if (data != null) {
                        if (data.has("display_name")) {
                            // Use custom display name if provided
                            lore.add(Component.literal("§7" + data.get("display_name").getAsString()));
                        } else if (data.has("id")) {
                            // Fall back to item ID if no display name
                            String itemId = data.get("id").getAsString();
                            String[] parts = itemId.split(":");
                            String itemName = parts[parts.length - 1];
                            itemName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
                            lore.add(Component.literal("§7" + itemName));
                        }
                    }
                    break;
            }
        }
        return lore;
    }

    private static Button createRewardButton(ServerPlayer player, PlayerBattlePass pass, BattlePassTier tier, int level, boolean isPremium) {
        Reward reward = isPremium ? tier.getPremiumReward() : tier.getFreeReward();
        ItemStack displayItem = isPremium ? tier.getPremiumRewardItem(player.level().registryAccess()) : tier.getFreeRewardItem(player.level().registryAccess());

        if (displayItem == null || displayItem.isEmpty()) {
            displayItem = new ItemStack(Items.STONE);
        }

        return GooeyButton.builder()
                .display(displayItem)
                .with(DataComponents.CUSTOM_NAME, Component.literal(isPremium ? "§6Premium Reward" : "§aFree Reward"))
                .with(DataComponents.LORE, new ItemLore(getRewardLore(tier, isPremium)))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    if (level > pass.getLevel()) {
                        player.sendSystemMessage(Component.literal(String.format(Constants.MSG_LEVEL_NOT_REACHED, level)));
                        return;
                    }

                    if (isPremium && !pass.isPremium()) {
                        player.sendSystemMessage(Component.literal(Constants.MSG_NOT_PREMIUM));
                        return;
                    }

                    if (CobblePass.battlePass.claimReward(player, level, isPremium)) {
                        player.sendSystemMessage(Component.literal(String.format(Constants.MSG_REWARD_CLAIM, level)));
                        showBattlePassInfo(player); // Refresh UI
                    }
                })
                .build();
    }

private static Button createStatusButton(PlayerBattlePass pass, BattlePassTier tier, int level, boolean isPremium) {
    ItemStack statusGlass;
    List<Component> statusLore = new ArrayList<>();
    String statusName = "§3Level " + level + " Status";

    if (isPremium) {
        // --- Premium Track Logic ---
        if (!tier.hasPremiumReward()) {
            // No premium reward at this tier, so it's an empty slot.
            return new PlaceholderButton();
        }

        if (!pass.isPremium()) {
            statusGlass = new ItemStack(Items.RED_STAINED_GLASS_PANE);
            statusLore.add(Component.literal("§cRequires Premium Pass"));
            statusLore.add(Component.literal("§7Purchase with §e/bp premium buy"));
        } else if (level > pass.getLevel()) {
            statusGlass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
            statusLore.add(Component.literal("§7Not Reached"));
        } else if (pass.hasClaimedPremiumReward(level)) {
            statusGlass = new ItemStack(Items.ORANGE_STAINED_GLASS_PANE);
            statusLore.add(Component.literal("§6Claimed"));
        } else {
            statusGlass = new ItemStack(Items.GREEN_STAINED_GLASS_PANE);
            statusLore.add(Component.literal("§aAvailable to Claim"));
        }

    } else {
        // --- Free Track Logic ---
        if (!tier.hasFreeReward()) {
            // No free reward at this tier.
            return new PlaceholderButton();
        }

        if (level > pass.getLevel()) {
            statusGlass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
            statusLore.add(Component.literal("§7Not Reached"));
        } else if (pass.hasClaimedFreeReward(level)) {
            statusGlass = new ItemStack(Items.ORANGE_STAINED_GLASS_PANE);
            statusLore.add(Component.literal("§6Claimed"));
        } else {
            statusGlass = new ItemStack(Items.GREEN_STAINED_GLASS_PANE);
            statusLore.add(Component.literal("§aAvailable to Claim"));
        }
    }

    return GooeyButton.builder()
            .display(statusGlass)
            .with(DataComponents.CUSTOM_NAME, Component.literal(statusName))
            .with(DataComponents.LORE, new ItemLore(statusLore))
            .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();
}

    public static void showBattlePassInfo(ServerPlayer player) {
        PlayerBattlePass pass = CobblePass.battlePass.getPlayerPass(player);
        
        // Create info button showing level, XP and time remaining
        int currentXP = pass.getXP();
        int xpForNext;
        if (CobblePass.config.getXpProgression().getMode().equalsIgnoreCase("MANUAL")) {
            xpForNext = CobblePass.config.getXpProgression().getManualXpForLevel(pass.getLevel() + 1);
        } else {
            xpForNext = (int)(CobblePass.config.getXpProgression().getXpPerLevel() * Math.pow(CobblePass.config.getXpProgression().getXpMultiplier(), pass.getLevel() - 1));
        }
        List<Component> infoLore = new ArrayList<>(Arrays.asList(
            Component.literal(String.format("§3Level: §f%d", pass.getLevel())),
            Component.literal(String.format("§3XP: §f%d§7/§f%d", currentXP, xpForNext))
        ));
        
        if (CobblePass.config.isSeasonActive()) {
            long timeLeft = CobblePass.config.getSeasonEndTime() - System.currentTimeMillis();
            if (timeLeft > 0) {
                infoLore.add(Component.literal("§3Time Remaining: §b" + formatTimeRemaining(timeLeft)));
            }
        }

        GooeyButton infoButton = GooeyButton.builder()
            .display(new ItemStack(Items.EXPERIENCE_BOTTLE))
            .with(DataComponents.CUSTOM_NAME, Component.literal("§bBattle Pass Progress"))
            .with(DataComponents.LORE, new ItemLore(infoLore))
            .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        // Create premium status button
        ItemStack premiumDisplay = pass.isPremium() ? new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item()) : new ItemStack(PokeBalls.INSTANCE.getPREMIER_BALL().item());
        List<Component> premiumLore = new ArrayList<>();
        if (pass.isPremium()) {
            if (CobblePass.config.isSeasonActive()) {
                premiumLore.add(Component.literal("§3Season " + CobblePass.config.getCurrentSeason()));
            } else {
                premiumLore.add(Component.literal("§cNo active season"));
            }
        } else {
            premiumLore.add(Component.literal("§cInactive"));
            premiumLore.add(Component.literal("§7Click to upgrade!"));
        }
        
        GooeyButton premiumButton = GooeyButton.builder()
            .display(premiumDisplay)
            .with(DataComponents.CUSTOM_NAME, Component.literal("§6Premium Status"))
            .with(DataComponents.LORE, new ItemLore(premiumLore))
            .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .onClick(action -> {
                if (!pass.isPremium()) {
                    player.sendSystemMessage(Component.literal("§7Use §f/battlepass premium §7to unlock premium rewards"));
                }
            })
            .build();

        // Create empty background button
        Button background = new PlaceholderButton();

        Map<Integer, BattlePassTier> tiers = CobblePass.battlePass.getTiers();
        int totalTiers = tiers.size();
        
        List<LinkedPage> pages = new ArrayList<>();
        int buttonsPerPage = 7;
        int totalPages = (int) Math.ceil((double) totalTiers / buttonsPerPage);

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            ChestTemplate template = ChestTemplate.builder(6)
                .fill(background)
                .set(0, 4, infoButton)
                .set(0, 8, premiumButton)
                .set(2, 0, GooeyButton.builder().display(new ItemStack(PokeBalls.INSTANCE.getPREMIER_BALL().item())).with(DataComponents.CUSTOM_NAME, Component.literal("§aFree Rewards")).build())
                .set(4, 0, GooeyButton.builder().display(new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item())).with(DataComponents.CUSTOM_NAME, Component.literal("§6Premium Rewards")).build())
                .build();

            int startIdx = pageNum * buttonsPerPage;
            int endIdx = Math.min(startIdx + buttonsPerPage, totalTiers);

            for (int i = startIdx; i < endIdx; i++) {
                int column = 1 + (i - startIdx);
                final int level = i + 1;
                BattlePassTier tier = tiers.get(level);
                if (tier == null) continue;

                if (tier.hasFreeReward()) {
                    template.set(2, column, createRewardButton(player, pass, tier, level, false));
                } else {
                    template.set(2, column, new PlaceholderButton());
                }

                template.set(3, column, createStatusButton(pass, tier, level, false)); // Status for Free reward

                if (tier.hasPremiumReward()) {
                    template.set(4, column, createRewardButton(player, pass, tier, level, true));
                } else {
                    template.set(4, column, new PlaceholderButton());
                }

                template.set(5, column, createStatusButton(pass, tier, level, true)); // Status for Premium reward
            }

            LinkedPage page = LinkedPage.builder()
                .template(template)
                .title("§3Battle Pass")
                .build();

            pages.add(page);
        }

        // Link pages together
        for (int i = 0; i < pages.size(); i++) {
            LinkedPage current = pages.get(i);
            if (i > 0) {
                current.setPrevious(pages.get(i - 1));
            }
            if (i < pages.size() - 1) {
                current.setNext(pages.get(i + 1));
            }

            // Add navigation buttons
            ChestTemplate template = (ChestTemplate) current.getTemplate();
            if (current.getPrevious() != null) {
                Button prevBtn = LinkedPageButton.builder()
                    .display(new ItemStack(Items.ARROW))
                    .with(DataComponents.CUSTOM_NAME, Component.literal("§f← Previous Page"))
                    .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                    .linkType(LinkType.Previous)
                    .build();
                template.set(5, 0, prevBtn);
            }
            if (current.getNext() != null) {
                Button nextBtn = LinkedPageButton.builder()
                    .display(new ItemStack(Items.ARROW))
                    .with(DataComponents.CUSTOM_NAME, Component.literal("§fNext Page →"))
                    .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                    .linkType(LinkType.Next)
                    .build();
                template.set(5, 8, nextBtn);
            }
        }

        // Open the first page
        if (!pages.isEmpty()) {
            UIManager.openUIForcefully(player, pages.get(0));
        }
    }
}
