package com.cobblemon.mdks.cobblepass.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.BattlePassTier;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.command.subcommand.AddLevelsCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.AddXPCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.ClaimCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.CreateCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.DeleteCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.ManagePremiumCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.PremiumCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.ReloadCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.SeasonCommand;
import com.cobblemon.mdks.cobblepass.data.Reward;
import com.cobblemon.mdks.cobblepass.util.BaseCommand;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Permissions;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

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
                        String[] parts = itemId.split(":");
                        String itemName = parts[parts.length - 1].replace("_", " ");
                        itemName = Arrays.stream(itemName.split(" "))
                                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                .collect(Collectors.joining(" "));
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
                            String speciesName = data.get("species").getAsString();
                            speciesName = speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1);
                            lore.add(Component.literal(speciesName));
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
                            lore.add(Component.literal("§7" + data.get("display_name").getAsString()));
                        } else if (data.has("id")) {
                            String itemId = data.get("id").getAsString();
                            String[] parts = itemId.split(":");
                            String itemName = parts[parts.length - 1].replace("_", " ");
                            itemName = Arrays.stream(itemName.split(" "))
                                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                    .collect(Collectors.joining(" "));
                            lore.add(Component.literal("§7" + itemName));
                        }
                    }
                    break;
            }
        }
        return lore;
    }

    private static Button createRewardButton(ServerPlayer player, PlayerBattlePass pass, BattlePassTier tier, int level, boolean isPremium, int pageNum) {
        Reward reward = isPremium ? tier.getPremiumReward() : tier.getFreeReward();
        ItemStack displayItem = isPremium ? tier.getPremiumRewardItem(player.level().registryAccess()) : tier.getFreeRewardItem(player.level().registryAccess());

        if (displayItem == null || displayItem.isEmpty()) {
            displayItem = new ItemStack(Items.BARRIER);
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
                        showBattlePassInfo(player, pageNum);
                    }
                })
                .build();
    }

    private static Button createStatusButton(PlayerBattlePass pass, BattlePassTier tier, int level, boolean isPremium) {
        ItemStack statusGlass;
        List<Component> statusLore = new ArrayList<>();
        String statusName = "§3Level " + level + " Status";

        if (isPremium) {
            if (!tier.hasPremiumReward()) {
                // This case should ideally not be reached due to the new checks, but as a fallback:
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
                statusGlass = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
                statusLore.add(Component.literal("§aAvailable to Claim"));
            }

        } else {
            if (!tier.hasFreeReward()) {
                // This case should ideally not be reached due to the new checks, but as a fallback:
                return new PlaceholderButton();
            }

            if (level > pass.getLevel()) {
                statusGlass = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
                statusLore.add(Component.literal("§7Not Reached"));
            } else if (pass.hasClaimedFreeReward(level)) {
                statusGlass = new ItemStack(Items.ORANGE_STAINED_GLASS_PANE);
                statusLore.add(Component.literal("§6Claimed"));
            } else {
                statusGlass = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
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
        showBattlePassInfo(player, 0);
    }

    public static void showBattlePassInfo(ServerPlayer player, int pageToShow) {
        PlayerBattlePass pass = CobblePass.battlePass.getPlayerPass(player);

        Button background = GooeyButton.builder()
                .display(new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§7 "))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .build();

        List<Component> xpInfoLore = new ArrayList<>();
        xpInfoLore.add(Component.literal("§7Earn XP by performing these actions:"));
        xpInfoLore.add(Component.literal(""));
        if (CobblePass.config.getCatchXP() > 0) {
            xpInfoLore.add(Component.literal(String.format("  §b\u2022 Catch Pokémon: §e+%d XP", CobblePass.config.getCatchXP())));
        }
        if (CobblePass.config.getDefeatXP() > 0) {
            xpInfoLore.add(Component.literal(String.format("  §b\u2022 Defeat Pokémon: §e+%d XP", CobblePass.config.getDefeatXP())));
        }
        if (CobblePass.config.getEvolveXP() > 0) {
            xpInfoLore.add(Component.literal(String.format("  §b\u2022 Evolve Pokémon: §e+%d XP", CobblePass.config.getEvolveXP())));
        }
        if (CobblePass.config.getHatchXP() > 0) {
            xpInfoLore.add(Component.literal(String.format("  §b\u2022 Hatch Eggs: §e+%d XP", CobblePass.config.getHatchXP())));
        }
        if (CobblePass.config.getTradeXP() > 0) {
            xpInfoLore.add(Component.literal(String.format("  §b\u2022 Trade Pokémon: §e+%d XP", CobblePass.config.getTradeXP())));
        }

        GooeyButton xpInfoButton = GooeyButton.builder()
                .display(new ItemStack(Items.EXPERIENCE_BOTTLE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§aHow to Earn XP"))
                .with(DataComponents.LORE, new ItemLore(xpInfoLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .build();

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
                infoLore.add(Component.literal(""));
                infoLore.add(Component.literal("§3Time Remaining: §b" + formatTimeRemaining(timeLeft)));
            }
        }

        GooeyButton infoButton = GooeyButton.builder()
                .display(new ItemStack(Items.NETHER_STAR))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§bBattle Pass Progress"))
                .with(DataComponents.LORE, new ItemLore(infoLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .build();

        ItemStack premiumDisplay = pass.isPremium() ? new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item()) : new ItemStack(PokeBalls.INSTANCE.getPREMIER_BALL().item());
        List<Component> premiumLore = new ArrayList<>();
        if (pass.isPremium()) {
            premiumLore.add(Component.literal("§aYou have the Premium Pass!"));
            if (CobblePass.config.isSeasonActive()) {
                premiumLore.add(Component.literal("§3Season " + CobblePass.config.getCurrentSeason()));
            } else {
                premiumLore.add(Component.literal("§cNo active season"));
            }
        } else {
            premiumLore.add(Component.literal("§cPremium Pass Inactive"));
            premiumLore.add(Component.literal("§7Click to learn more!"));
        }

        GooeyButton premiumButton = GooeyButton.builder()
                .display(premiumDisplay)
                .with(DataComponents.CUSTOM_NAME, Component.literal("§6Premium Status"))
                .with(DataComponents.LORE, new ItemLore(premiumLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    if (!pass.isPremium()) {
                        player.closeContainer();
                        player.sendSystemMessage(Component.literal("§7Use §f/battlepass premium §7to unlock premium rewards."));
                    }
                })
                .build();

        Map<Integer, BattlePassTier> tiers = CobblePass.battlePass.getTiers();
        int totalTiers = tiers.values().stream().mapToInt(BattlePassTier::getLevel).max().orElse(0);

        List<LinkedPage> pages = new ArrayList<>();
        int buttonsPerPage = 7;
        int totalPages = (int) Math.ceil((double) totalTiers / buttonsPerPage);

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            ChestTemplate template = ChestTemplate.builder(6)
                    .fill(background)
                    .set(0, 0, xpInfoButton)
                    .set(0, 4, infoButton)
                    .set(0, 8, premiumButton)
                    .set(2, 0, GooeyButton.builder().display(new ItemStack(PokeBalls.INSTANCE.getPREMIER_BALL().item())).with(DataComponents.CUSTOM_NAME, Component.literal("§aFree Rewards")).build())
                    .set(4, 0, GooeyButton.builder().display(new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item())).with(DataComponents.CUSTOM_NAME, Component.literal("§6Premium Rewards")).build())
                    .build();

            int startLevel = (pageNum * buttonsPerPage) + 1;
            int endLevel = Math.min(startLevel + buttonsPerPage, totalTiers + 1);

            for (int level = startLevel; level < endLevel; level++) {
                int column = 1 + (level - startLevel);
                BattlePassTier tier = tiers.get(level);
                if (tier == null) continue;

                // FIX: Check if the tier has a reward before attempting to place a button.
                // This prevents placing a PlaceholderButton over our background fill.
                if (tier.hasFreeReward()) {
                    template.set(2, column, createRewardButton(player, pass, tier, level, false, pageNum));
                    template.set(3, column, createStatusButton(pass, tier, level, false));
                }

                // FIX: Same check for the premium reward.
                if (tier.hasPremiumReward()) {
                    template.set(4, column, createRewardButton(player, pass, tier, level, true, pageNum));
                    template.set(5, column, createStatusButton(pass, tier, level, true));
                }
            }

            LinkedPage page = LinkedPage.builder()
                    .template(template)
                    .title("§3Battle Pass")
                    .build();

            pages.add(page);
        }

        for (int i = 0; i < pages.size(); i++) {
            LinkedPage current = pages.get(i);
            if (i > 0) {
                current.setPrevious(pages.get(i - 1));
            }
            if (i < pages.size() - 1) {
                current.setNext(pages.get(i + 1));
            }

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

        if (!pages.isEmpty()) {
            int targetPage = Math.max(0, Math.min(pageToShow, pages.size() - 1));
            UIManager.openUIForcefully(player, pages.get(targetPage));
        }
    }
}
